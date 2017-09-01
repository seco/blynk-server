import {API_URL} from 'services/API';

export function WidgetHistoryByPinFetch(data) {

  /*
  */

  if (!data.deviceId)
    throw new Error('Missing device id parameter for widget HistoryByPin fetch');

  if (!data.widgetId)
    throw new Error('Missing widget id parameter for widget HistoryByPin fetch');

  if (!data.pin)
    throw new Error('Missing pin parameter for widget HistoryByPin fetch');


  return {
    type: 'API_WIDGETS_HISTORY_BY_PIN',
    value: data,
    payload: {
      request: {
        method: 'get',
        url: API_URL.widgets().historyByPin(data)
      }
    }
  };
}
