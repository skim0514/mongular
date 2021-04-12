import { Component, OnInit } from '@angular/core';
import { WebsitesService} from "../../services/websites.service";
import {CompareComponent} from "../compare/compare.component";
import {FormControl, Validators} from "@angular/forms";
import {DomSanitizer} from "@angular/platform-browser";


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

  constructor(private websitesService: WebsitesService, private sanitizer: DomSanitizer) { }

  ngOnInit(): void {
  }

  reset(): void {
    this.double = false;
    this.activated = false;
    this.date1 = null;
    this.date2 = null;
  }

  getDate1(): string {
    if (this.date1 == null) console.log("HELOPPPPPPPP")
    console.log(this.date1)
    var url = "http://118.67.133.84:8085/api/comparison?web=http://crdclub4wraumez4.onion/&prev=" + this.date1.replace(/-/g,"");
    return url as string;
  }

  getDate2(): string{
    if (this.date2 == null) console.log("HELOPPPPPPPP")
    console.log(this.date2)
    console.log(this.date1)
    var url = "http://118.67.133.84:8085/api/comparison?web=http://crdclub4wraumez4.onion/&prev=" + this.date2.replace(/-/g,"")
    + "&next=" + this.date1.replace(/-/g,"");
    return url as string;
  }

  setDates(): void {
    this.activated = false;
    this.double = false;
    this.websitesService.getDates(this.website).subscribe(
      data => {
        this.dates = data as string[];
        this.date2 = this.dates[this.dates.length - 1]
        console.log(data);
      },
    )
  }


  getChanges(): void {
    if(this.date1 == null) return;

    this.activated = true;
    this.double = false;
  }

  getComparison(): void {
    if(this.date1 == null || this.date2 == null) return;

    this.activated = true;
    this.double = true;
  }
}
