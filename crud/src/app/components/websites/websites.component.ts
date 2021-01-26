import { Component, OnInit } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import {DomSanitizer} from '@angular/platform-browser';
// import { FileService } from './file.service';
import { Router, Params } from '@angular/router';

import { Observable } from 'rxjs';

@Component({
  selector: 'app-websites',
  templateUrl: './websites.component.html',
  styleUrls: ['./websites.component.css']
})
export class WebsitesComponent implements OnInit {
  home = true;
  title = 'http-interceptor-example';
  currpage;
  totalAngularPackages;
  htmlfile;
  urls;
  domain;

  constructor(private http: HttpClient,
              private sanitizer:DomSanitizer,
              private router: Router) {
    this.currpage = router.url
  }



  url_parser(url: string) {
    var regex = /[/]/g;
    this.domain = url.split('/')[1];
    let new_url = url.replace('/websites', '');
    console.log(this.domain);
    return "proxy_" + new_url.replace('/', '').replace(regex, '_');
  }

  href_parser(file: string) {
    var lines = file.split('\n');
    var num = lines.length;
    var i;
    var a_pattern = /<a [^>]*href="[^"]*"[^>]*>/gm
    var as = file.match(a_pattern);
    for (i = 0; i < as.length; i++) {
      // console.log(as);
      var link = as[i]
      var path = as[i];

      // var path1 = path.replace(/.*href=("[^"]*").*/, '$1');
      // console.log(path1)
      var path2 = path.replace(/.*href="([^"]*)".*/, '$1');
      console.log(path2);
      var long = link;
      console.log(link);
      long = long.replace(path2, 'http://localhost:8081/websites/www.s2wlab.com/' + path2)
      file = file.replace(link, long);
    }
    return file;
  }



  image_parser(file: string) {
    let lines = file.split('\n');
    let num = lines.length;
    let i;
    let image_pattern = /<img [^>]*src="[^"]*"[^>]*>/gm
    let images = file.match(image_pattern);
    for (i = 0; i < images.length; i++) {
      // console.log(as);
      let link = images[i]
      let path = images[i];

      // var path1 = path.replace(/.*href=("[^"]*").*/, '$1');
      // console.log(path1)
      let path2 = path.replace(/.*src="([^"]*)".*/, '$1');
      console.log(path2);
      let long = link;
      console.log(link);
      long = long.replace(path2, 'http://localhost:8082/' + path2)
      file = file.replace(link, long);
    }
    return file;
  }

  script_parser(file: string) {
    var lines = file.split('\n');
    var num = lines.length;
    var i;
    var script_pattern = /<script [^>]*src="[^"]*"[^>]*>/gm
    var scripts = file.match(script_pattern);
    for (i = 0; i < scripts.length; i++) {
      // console.log(as);
      var link = scripts[i]
      var path = scripts[i];

      // var path1 = path.replace(/.*href=("[^"]*").*/, '$1');
      // console.log(path1)
      var path2 = path.replace(/.*src="([^"]*)".*/, '$1');
      console.log(path2);
      var long = link;
      console.log(link);
      long = long.replace(path2, 'http://localhost:8082/' + path2)
      file = file.replace(link, long);
    }
    return file;
  }

  css_parser(file: string) {
    var lines = file.split('\n');
    var num = lines.length;
    var i;
    var script_pattern = /<link [^>]*href="[^"]*"[^>]*>/gm
    var scripts = file.match(script_pattern);
    for (i = 0; i < scripts.length; i++) {
      // console.log(as);
      var link = scripts[i]
      var path = scripts[i];

      // var path1 = path.replace(/.*href=("[^"]*").*/, '$1');
      // console.log(path1)
      var path2 = path.replace(/.*href="([^"]*)".*/, '$1');
      console.log(path2);
      var long = link;
      console.log(link);
      long = long.replace(path2, 'http://localhost:8082/' + path2)
      file = file.replace(link, long);
    }
    return file;
  }

  ngOnInit(): void {
    this.http.get('http://localhost:8080/api/websites/www.s2wlab.com', {responseType: 'text'})
      .subscribe(res => {
        // var image = this.image_parser(res);
        // var scripts = this.script_parser(image);
        // var href = this.href_parser(scripts);
        // var css = this.css_parser(href);

        //this.totalAngularPackages = this.sanitizer.bypassSecurityTrustHtml(res)
        this.totalAngularPackages = this.sanitizer.bypassSecurityTrustHtml(res);

        //todo
      });

  }
}
