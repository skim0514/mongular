import { Component } from '@angular/core';
import {DomSanitizer} from "@angular/platform-browser";
import {HttpClient} from "@angular/common/http";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  title = 'crud';
  mainHtml;
  constructor(private http:HttpClient, private sanitizer:DomSanitizer) {

  }
  ngOnInit() {
    this.http.get('http://localhost:8085/api/websites?web=https://www.s2wlab.com/about.html', {responseType:'text'}).subscribe(res=>
    {
      this.mainHtml = this.sanitizer.bypassSecurityTrustHtml(res);
    })
  }
}
