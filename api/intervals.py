import requests

HOST = 'https://intervals.icu'
USERNAME = 'API_KEY'


class IntervalsApi:
    def __init__(self, api_key, athlete_id):
        self.api_key = api_key
        self.athlete_id = athlete_id

    def test(self):
        resp = requests.get(f'{HOST}/api/v1/athlete/{self.athlete_id}/calendars', auth=(USERNAME, self.api_key))
        resp.raise_for_status()
        return resp.json()
