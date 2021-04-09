import { Component, OnInit } from '@angular/core';
import { WebsitesService} from "../../services/websites.service";


@Component({
  selector: 'app-dates',
  templateUrl: './dates.component.html',
  styleUrls: ['./dates.component.css']
})
export class DatesComponent implements OnInit {
  baseDate?: string;
  dates?: string[];
  website?: string;

  constructor(private websitesService: WebsitesService) { }

  ngOnInit(): void {
  }

  setBase(date: string): void {
    this.baseDate = date;
  }

  setDates(): void {
    this.websitesService.getDates(this.website).subscribe(
      data => {
        this.dates = data as string[];
        console.log(data);
      },
    )
  }
}
