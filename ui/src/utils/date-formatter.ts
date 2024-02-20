import { formatDate as angularFormatDate } from "@angular/common";

const DATE_FORMAT = 'yyyy-MM-dd'
const LOCALE = 'en-US'

export function formatDate(date: Date): string {
  return angularFormatDate(date, DATE_FORMAT, LOCALE)
}
