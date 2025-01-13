#!/usr/bin/env python3
import csv
import urllib.request
from os import path
import pathlib
import hashlib
import argparse
import shutil
from subprocess import check_output, CalledProcessError, STDOUT

class Jdk:
    def __init__(self, variant, version, os, arch, url, checksum):
        self.variant = variant
        self.version = version
        self.os = os
        self.arch = arch
        self.url = url
        self.checksum = checksum
        self.zip_filename = "{}-{}-{}-{}.zip".format(variant, version, os, arch)
        self.zip_path = path.join(path.dirname(__file__), "jdk_zips")
        self.zip_full_path = path.join(self.zip_path, self.zip_filename)
        self.unzip_path = path.join(path.dirname(__file__), "jdks", self.os, self.arch)

    def download(self):
        print("Checking if {} already exists".format(self.zip_filename))
        if path.exists(path.join(self.zip_path, self.zip_filename)):
            print("{} already exists".format(self.zip_filename))
            if self.verify_checksum():
                print("Checksum matches for {}, skipping download".format(self.zip_filename))
                return
            else:
                print("Checksum does not match for {}, downloading again".format(self.zip_filename))
        pathlib.Path(self.zip_path).mkdir(parents=True, exist_ok=True)
        print("Downloading {}...".format(self.url))
        urllib.request.urlretrieve(self.url, self.zip_full_path)
        print("Downloaded {} to {}".format(self.url, self.zip_full_path))
        print("Verifying checksum for {}".format(self.zip_filename))
        if self.verify_checksum():
            print("Checksum verified for {}".format(self.zip_filename))
            return
        else :
            raise Exception("Checksum does not match for {}".format(self.zip_filename))
            return

    def verify_checksum(self):
        if not path.exists(self.zip_full_path):
            return False
        with open (self.zip_full_path, "rb") as f:
            data = f.read()
            sha256 = hashlib.sha256(data).hexdigest()
        return self.checksum == sha256

    def unzip(self):
        print("Unzipping {}".format(self.zip_filename))
        pathlib.Path(self.unzip_path).mkdir(parents=True, exist_ok=True)
        try:
            check_output(['unzip', '-q', self.zip_full_path, '-d', self.unzip_path], stderr=STDOUT)
        except CalledProcessError as err:
            print(err.output)
            raise err
        for item in pathlib.Path(self.unzip_path).iterdir():
            if item.is_dir():
                shutil.move(path.join(self.unzip_path, item.name), path.join(self.unzip_path, "jdk"))
        print("Unzipped {} to {}".format(self.zip_filename, self.unzip_path))

    def fix_permissions(self):
        print("Fixing permissions for {}".format(self.unzip_path))
        check_output(['chmod', '-R', 'a+rw', self.unzip_path], stderr=STDOUT)
        print("Fixed permissions for {}".format(self.unzip_path))


jdks = []

with open(path.join(path.dirname(__file__), 'jdks.csv'), 'r') as csv_file:
    csv_reader = csv.DictReader(csv_file)
    for line in csv_reader:
        jdks.append(Jdk(line['variant'], line['version'], line['os'], line['arch'], line['url'], line['checksum']))

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("action", help="Action to perform", choices=["clean", "download", "unzip"], nargs='+')
    parser.add_argument("-o", "--os", help="OS", choices=["mac", "linux", "win"], nargs='+', required=True)
    parser.add_argument("-a", "--arch", help="Architectures", choices=["x64", "arm64"], nargs='+', required=True)
    args = parser.parse_args()

    filtered_jdks = [jdk for jdk in jdks if jdk.arch in args.arch and jdk.os in args.os]

    print("Running: {}".format(",".join(args.action)))
    if "clean" in args.action:
        print("Cleaning jdks and zips...")
        if not path.exists(path.join(path.dirname(__file__), "jdks")):
            print("No jdks to clean")
        else:
            shutil.rmtree(path.join(path.dirname(__file__), "jdks"))
        if not path.exists(path.join(path.dirname(__file__), "jdk_zips")):
            print("No jdk zips to clean")
        else:
            shutil.rmtree(path.join(path.dirname(__file__), "jdk_zips"))

    if "download" in args.action:
        print("Downloading jdks...")
        for jdk in filtered_jdks:
            jdk.download()

    if "unzip" in args.action:
        print("Unzipping jdks...")
        for jdk in filtered_jdks:
            jdk.unzip()
            jdk.fix_permissions()
