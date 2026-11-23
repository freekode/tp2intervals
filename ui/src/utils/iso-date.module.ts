import {Injectable, NgModule} from '@angular/core';
import {DateAdapter, MAT_DATE_FORMATS, MAT_NATIVE_DATE_FORMATS, NativeDateAdapter} from '@angular/material/core';
import {formatDate} from './date-formatter';

const ISO_DATE = 'yyyy-MM-dd';

@Injectable()
export class IsoDateAdapter extends NativeDateAdapter {
  override parse(value: unknown): Date | null {
    if (value === '' || value == null) return null;
    if (value instanceof Date) return value;
    if (typeof value !== 'string' || !/^\d{4}-\d{2}-\d{2}$/.test(value)) return this.invalid();
    const [year, month, day] = value.split('-').map(Number);
    const date = new Date(0);
    date.setFullYear(year, month - 1, day);
    date.setHours(0, 0, 0, 0);
    return formatDate(date) === value ? date : this.invalid();
  }

  override deserialize(value: unknown): Date | null {
    return typeof value === 'string' ? this.parse(value) : super.deserialize(value);
  }

  override format(date: Date, displayFormat: Object): string {
    return displayFormat === ISO_DATE ? formatDate(date) : super.format(date, displayFormat);
  }
}

@NgModule({
  providers: [
    {provide: DateAdapter, useClass: IsoDateAdapter},
    {
      provide: MAT_DATE_FORMATS,
      useValue: {
        parse: {dateInput: ISO_DATE},
        display: {...MAT_NATIVE_DATE_FORMATS.display, dateInput: ISO_DATE, dateA11yLabel: ISO_DATE},
      },
    },
  ],
})
export class IsoDateModule {}
