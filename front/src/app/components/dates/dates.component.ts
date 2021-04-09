import { Component, OnInit } from '@angular/core';
import { WebsitesService} from "../../services/websites.service";

@Component({
  selector: 'app-dates',
  templateUrl: './dates.component.html',
  styleUrls: ['./dates.component.css']
})
export class DatesComponent implements OnInit {
  dates?: string[];
  website?: string;

  constructor(private websitesService: WebsitesService) { }

  ngOnInit(): void {
  }

  setActiveDates(tutorial: string): void {
    this.website = tutorial;
    this.setDates();
  }

  setDates() {
    this.websitesService.getDates(this.website).subscribe(
      data => {
        this.dates = data as string[];
        console.log(data);
      },
    )
  }
}
