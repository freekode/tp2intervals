const requiredConfigKeys = ['intervals.api-key', 'intervals.athlete-id']

export class Platform {
  static INTERVALS = {key: 'INTERVALS', title: 'Intervals.icu'}
  static TRAINING_PEAKS = {key: 'TRAINING_PEAKS', title: 'TrainingPeaks'}
  static TRAINER_ROAD = {key: 'TRAINER_ROAD', title: 'TrainerRoad'}
  static platforms = [
    this.INTERVALS, this.TRAINING_PEAKS, this.TRAINER_ROAD
  ]

  static DIRECTION_TP_INT = {
    sourcePlatform: this.TRAINING_PEAKS.key, targetPlatform: this.INTERVALS.key
  }
  static DIRECTION_INT_TP = {
    sourcePlatform: this.INTERVALS.key, targetPlatform: this.TRAINING_PEAKS.key
  }
  static DIRECTION_TR_INT = {
    sourcePlatform: this.TRAINER_ROAD.key, targetPlatform: this.INTERVALS.key
  }
  static DIRECTION_TR_TP = {
    sourcePlatform: this.TRAINER_ROAD.key, targetPlatform: this.TRAINING_PEAKS.key
  }

  static getTitle(key) {
    return this.platforms.find(platform => platform.key === key)?.title
  }
}
