export class TrainingTypes {
  static trainingTypes = [
    {title: "Ride", value: "BIKE"},
    {title: "MTB", value: "MTB"},
    {title: "Virtual Ride", value: "VIRTUAL_BIKE"},
    {title: "Run", value: "RUN"},
    {title: "Swim", value: "SWIM"},
    {title: "Walk", value: "WALK"},
    {title: "Weight Training", value: "WEIGHT"},
    {title: "Any other", value: "UNKNOWN"},
  ]

  static getTitle(value: string) {
    return this.trainingTypes.find(type => type.value === value)?.title
  }
}
