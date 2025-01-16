export class TrainerRoadTrainingTypes {
  static trainingTypes = [
    {title: "Ride", value: "BIKE"},
    {title: "Virtual Ride", value: "VIRTUAL_BIKE"},
    {title: "Unknown", value: "UNKNOWN"},
  ]

  static getTitle(value: string) {
    return this.trainingTypes.find(type => type.value === value)?.title
  }
}
