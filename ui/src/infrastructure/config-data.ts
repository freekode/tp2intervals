export class ConfigData {
  tpAuthCookie: string
  trAuthCookie: string
  intervalsApiKey: string
  intervalsAthleteId: string

  constructor(tpAuthCookie: string, trAuthCookie: string, intervalsApiKey: string, intervalsAthleteId: string) {
    this.tpAuthCookie = tpAuthCookie;
    this.trAuthCookie = trAuthCookie;
    this.intervalsApiKey = intervalsApiKey;
    this.intervalsAthleteId = intervalsAthleteId;
  }

  ifFilled(): boolean {
    return !!this.intervalsApiKey && !!this.intervalsAthleteId
  }
}
