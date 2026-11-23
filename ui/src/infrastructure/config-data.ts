export const credentialKeys = ['intervals.api-key', 'training-peaks.auth-cookie', 'trainer-road.auth-cookie'];

export class ConfigData {
  config: { [id: string]: string | boolean | null; }


  constructor(config: { [id: string]: string; }, readonly storedCredentials: Record<string, boolean> = {}) {
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
    return !!this.config['intervals.athlete-id'] && !!this.storedCredentials['intervals.api-key'];
  }
}
