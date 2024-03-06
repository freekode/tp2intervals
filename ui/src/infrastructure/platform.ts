const requiredConfigKeys = ['intervals.api-key', 'intervals.athlete-id']

export class Platform {
  static INTERVALS = {key: 'INTERVALS', title: 'Intervals.icu'}
  static TRAINING_PEAKS = {key: 'TRAINING_PEAKS', title: 'TrainingPeaks'}
  static TRAINER_ROAD = {key: 'TRAINER_ROAD', title: 'TrainerRoad'}
}
