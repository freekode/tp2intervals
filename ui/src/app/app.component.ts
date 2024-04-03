import { Component, OnInit } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { UpdateService } from "infrastructure/update.service";
import { TopBarComponent } from "app/top-bar/top-bar.component";


@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    RouterOutlet,
    TopBarComponent
  ],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent implements OnInit {

  constructor(
    private updateService: UpdateService,
  ) {
  }

  ngOnInit(): void {
    this.updateService.init()
  }
}
