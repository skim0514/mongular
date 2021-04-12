import { Component, OnInit } from '@angular/core';
import { WebsitesService} from "../../services/websites.service";
import {CompareComponent} from "../compare/compare.component";
import {FormControl, Validators} from "@angular/forms";


@Component({
  selector: 'app-dates',
  templateUrl: './dates.component.html',
  styleUrls: ['./dates.component.css']
})
export class DatesComponent implements OnInit {
  date1?: string;
  date2?: string;
  activated?: boolean;
  content1?: string;
  content2?: string;
  double?: boolean;
  dates?: string[];
  website?: string;

  constructor(private websitesService: WebsitesService) { }

  ngOnInit(): void {
  }

  reset(): void {
    this.double = false;
    this.activated = false;
    this.date1 = null;
    this.date2 = null;
  }

  setDate1(date1: string) {
    this.date1 = date1.replace(/-/g,"");
  }

  setDate2(date2: string) {
    this.date2 = date2.replace(/-/g,"");
  }

  setDates(): void {
    this.activated = false;
    this.double = false;
    this.websitesService.getDates(this.website).subscribe(
      data => {
        this.dates = data as string[];
        console.log(data);
      },
    )
  }

  getChanges(): void {
    if(this.date1 == null) return;

    this.setDate1(this.date1);
    this.activated = true;
    this.double = false;
    this.websitesService.getChanges(this.website, this.date1).subscribe(
      data => {
        this.content1 = data as string;
        console.log(data);
      })
  }

  getComparison(): void {
    if(this.date1 == null || this.date2 == null) return;
    this.setDate1(this.date1);
    this.setDate2(this.date2);
    this.activated = true;
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
