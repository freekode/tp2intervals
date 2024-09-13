import {Injectable} from '@angular/core';
import {map, Observable} from "rxjs";
import {HttpClient} from "@angular/common/http";


@Injectable({
  providedIn: 'root'
})
export class GitHubClient {
  private static url = 'https://api.github.com';

  constructor(
    private httpClient: HttpClient
  ) {
  }

  getLatestRelease(): Observable<any> {
    return this.httpClient.get(`${GitHubClient.url}/repos/freekode/tp2intervals/releases/latest`).pipe(
      map(response => new Release(response)),
    )
  }
}

export class Release {
  version: string;
  url: string;

  constructor(json: any) {
    this.version = json['tag_name'].split('v')[1];
    this.url = json['html_url'];
  }
}
