import { Injectable } from '@angular/core';
import { HttpClient } from "@angular/common/http";
import { map, Observable } from 'rxjs';


@Injectable({
  providedIn: 'root'
})
export class LibraryClient {

  constructor(private httpClient: HttpClient) {
  }

  getLibraries(platform: string): Observable<any[]> {
    return this.httpClient.get(`/api/library-container`, {params: {platform}}).pipe(
      map(plans => (<any[]>plans))
    )
  }

  copyLibraryContainer(libraryContainer, newName, newStartDate, stepModifier, platformDirection): Observable<any> {
    return this.httpClient
      .post(`/api/library-container/copy`, {
        libraryContainer,
        newName,
        newStartDate,
        stepModifier,
        ...platformDirection
      })
  }
}
