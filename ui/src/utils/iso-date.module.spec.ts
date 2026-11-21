import {Injector, runInInjectionContext} from '@angular/core';
import {MAT_DATE_LOCALE} from '@angular/material/core';
import {IsoDateAdapter} from './iso-date.module';

describe('ISO calendar dates', () => {
  const injector = Injector.create({providers: [{provide: MAT_DATE_LOCALE, useValue: 'en-US'}]});
  const adapter = runInInjectionContext(injector, () => new IsoDateAdapter());

  it('parses and displays ISO dates in local calendar time', () => {
    const date = adapter.parse('2028-02-29')!;
    expect([date.getFullYear(), date.getMonth(), date.getDate()]).toEqual([2028, 1, 29]);
    expect(adapter.format(date, 'yyyy-MM-dd')).toBe('2028-02-29');
    expect(adapter.deserialize('2028-02-29')).toEqual(date);
  });

  it('rejects locale-specific, overflow and timestamp inputs', () => {
    for (const value of ['02/03/2026', '2026-02-29', '2026-2-03', '2026-02-03T00:00:00Z']) {
      expect(adapter.isValid(adapter.parse(value)!)).withContext(value).toBeFalse();
    }
    expect(adapter.parse('')).toBeNull();
  });
});
