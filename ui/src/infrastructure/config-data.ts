const requiredConfigKeys = ['intervals.api-key', 'intervals.athlete-id']

export class ConfigData {
  config: { [id: string]: string; }


  constructor(config: { [id: string]: string; }) {
    this.config = {}
    Object.keys(config).forEach(key => {
      if (!!config[key]) {
        this.config[key] = config[key]
      } else {
        this.config[key] = "-1"
      }
    })
  }

  hasRequiredConfig(): boolean {
    return Object.keys(this.config).filter(key => requiredConfigKeys.indexOf(key) > -1).length == 2
  }
}
