import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {RouterModule, Routes} from "@angular/router";
import {DatesComponent} from "./components/dates/dates.component";

const routes: Routes = [
  { path: '', redirectTo: 'compare', pathMatch: 'full' },
  { path: 'compare', component: DatesComponent },
];


@NgModule({
  declarations: [],
  imports: [
    RouterModule.forRoot(routes)
  ],
  exports: [RouterModule]
})
export class AppRoutingModule { }
