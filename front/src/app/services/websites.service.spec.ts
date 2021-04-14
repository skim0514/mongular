import { TestBed } from '@angular/core/testing';

import { WebsitesService } from './websites.service';

describe('WebsitesService', () => {
  let service: WebsitesService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(WebsitesService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
