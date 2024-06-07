const requiredConfigKeys = ['intervals.api-key', 'intervals.athlete-id']

export class ConfigData {
  config: { [id: string]: string | boolean | null; }


  constructor(config: { [id: string]: string; }) {
    this.config = {}
    Object.keys(config).forEach(key => {
      if (config[key] === '' || config[key] === null || config[key] === undefined) {
        this.config[key] = null
      } else if (config[key] === 'true') {
        this.config[key] = true
      } else if (config[key] === 'false') {
        this.config[key] = false
      } else {
        this.config[key] = config[key]
      }
    })
  }

  hasRequiredConfig(): boolean {
    return Object.keys(this.config).filter(key => requiredConfigKeys.indexOf(key) > -1).length == 2
  }
}
