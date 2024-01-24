import { collectionEventSchema } from '@app/models/collection-event';
import { PatientAdd, PatientUpdate, patientSchema } from '@app/models/patient';
import { httpClient } from './api';

export class PatientApi {
  static async getByPnumber(pnumber?: string) {
    if (!pnumber) {
      return null;
    }

    const response = await httpClient({
      method: 'GET',
      path: ['patients', pnumber],
      body: undefined,
      query: undefined
    });
    const result = await response.json();
    const patient = patientSchema.parse(result);
    return patient;
  }

  static async getPatientCollectionEvent(pnumber?: string, vnumber?: number) {
    if (!pnumber || !vnumber) {
      return null;
    }

    const response = await httpClient({
      method: 'GET',
      path: ['patients', pnumber, 'collection-events', vnumber],
      body: undefined,
      query: undefined
    });
    const result = await response.json();
    const cevent = collectionEventSchema.parse(result);
    return cevent;
  }

  static async add(patient: PatientAdd) {
    const response = await httpClient({
      method: 'POST',
      path: ['patients'],
      body: JSON.stringify(patient),
      query: undefined
    });
    const result = await response.json();
    return patientSchema.parse(result);
  }

  static async update(patient: PatientUpdate) {
    const data = {
      pnumber: patient.pnumber,
      createdAt: patient.createdAt,
      studyNameShort: patient.studyNameShort
    };
    const response = await httpClient({
      method: 'PUT',
      path: ['patients', patient.pnumber],
      body: JSON.stringify(data),
      query: undefined
    });
    const result = await response.json();
    return patientSchema.parse(result);
  }
}