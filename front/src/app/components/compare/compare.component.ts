import {Component, Inject, OnInit} from '@angular/core';
import {WebsitesService} from "../../services/websites.service";

@Component({
  selector: 'app-compare',
  templateUrl: './compare.component.html',
  styleUrls: ['./compare.component.css']
})
export class CompareComponent implements OnInit {
  dates: string[];
  date1?: string;
  date2?: string;
  website?: string;
  double?: boolean;
  content1?: string;
  content2?: string;

  constructor(private websitesService: WebsitesService) {
  }

  ngOnInit(): void {
  }

  setDates(dates: string[]) {
    this.dates = dates;
  }

  setDate1(date1: string) {
    this.date1 = date1.replace(/-/g,"");
  }

  setDate2(date2: string) {
    this.date2 = date2.replace(/-/g,"");
  }

  // getChanges(): void {
  //   this.double = false;
  //   this.websitesService.getChanges(this.website, this.date1).subscribe(
  //     data => {
  //       this.content1 = data;
  //   })
  // }

  getComparison(): void {
    this.double = true;
    this.websitesService.getComparison(this.website, this.date1, this.date2).subscribe(
      data => {
        this.content1 = data;
      })
    this.websitesService.getComparison(this.website, this.date2, this.date1).subscribe(
      data => {
        this.content2 = data;
      })
  }

}
