export class ConfigData {
  tpAuthCookie: string
  intervalsApiKey: string
  intervalsAthleteId: string

  constructor(tpAuthCookie: string, intervalsApiKey: string, intervalsAthleteId: string) {
    this.tpAuthCookie = tpAuthCookie;
    this.intervalsApiKey = intervalsApiKey;
    this.intervalsAthleteId = intervalsAthleteId;
  }

  ifFilled(): boolean {
    return !!this.tpAuthCookie && !!this.intervalsApiKey && !!this.intervalsAthleteId
  }
}
