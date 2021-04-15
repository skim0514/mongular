# mongular
___

<h3>Mongular is a website crawling & web mirroring service.

It allows users to build a database of websites and offline browse these websites. </h3> 

---


**Dependencies**: 
<li>Database: MongoDB</li>
<li>FrontEnd: Angular</li>
<li>BackEnd: Java Spring</li>

___
<h3>
How To Use: 
</h3>

1. Build Backend using mvn spring-boot:run
2. Crawl and get websites using API

---
<h3>
API Explanation
</h3>

* Post
  * */website* - crawl the given website from the link
    * Param 1: web - full website path
  * */tutorial* - adds metadata to database - used more as helper function
    * Param 1: tutorial - class storing metadata. 
* Get
  * */tutorials* - gets all tutorials stored in database
  * */tutorials/id/{id}* - get tutorial from id number
  * */comparison*  - compares website between two different dates.
    * Param 1: prev - date to compare with
    * Param 2: next (optional) - date to compare to
  * */websites* - gets parsed website from link and date if applicable
    * Param 1: web - absolute link to website
    * Param 2: date (optional) - date of link to get from - gets closest result
  * */websites/dates* - gets all days a website was crawled
    * Param 1: web - absolute link of website



