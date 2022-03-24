import argparse
import sys

import api


def main(arguments):
    intervals_api = api.IntervalsApi(arguments.intervals_api_key, arguments.athlete_id)
    test = intervals_api.test()
    print(test)


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='Configure services using API calls.')
    parser.add_argument('--athlete-id', '-a', required=True, help='Athlete ID on Intervals.icu')
    parser.add_argument('--intervals-api-key', '-i', required=True, help='API key for Intervals.icu')
    parser.add_argument('--workout-summary-file', '-w', required=True, help='Location of the workout summary exported from TrainingPeaks')
    args = parser.parse_args(sys.argv[1:])
    main(args)
