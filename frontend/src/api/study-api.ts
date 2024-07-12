import { annotationTypeSchema } from '@app/models/annotation-type';
import { catalougeStatusSchema } from '@app/models/catalogue-status';
import { sourceSpecimenTypeSchema } from '@app/models/source-specimen-type';
import { Status } from '@app/models/status';
import { studyNameSchema } from '@app/models/study';
import { z } from 'zod';
import { httpClient } from './api';

export class StudyApi {
  static async names(status?: Status) {
    const response = await httpClient({
      method: 'GET',
      path: ['studies', 'names'],
      body: undefined,
      query: { status },
    });
    const result = await response.json();
    const names = z.array(studyNameSchema).parse(result);
    return names;
  }

  static async catalogue(nameShort: string) {
    const response = await httpClient({
      method: 'POST',
      path: ['studies', 'catalogues', nameShort],
      body: undefined,
      query: undefined,
    });
    const location = response.headers.get('Location');
    if (!location) {
      throw new Error('invalid response for catalogue');
    }
    return location;
  }

  static async catalogueStatus(nameShort: string, id: string) {
    const response = await httpClient({
      method: 'GET',
      path: ['studies', 'catalogues', nameShort, id],
      body: undefined,
      query: undefined,
    });
    const json = await response.json();
    return catalougeStatusSchema.parse(json);
  }

  static async annotationTypes(nameshort: string, status?: Status) {
    const response = await httpClient({
      method: 'GET',
      path: ['studies', nameshort, 'annotation-types'],
      body: undefined,
      query: { status },
    });
    const result = await response.json();
    const types = z.array(annotationTypeSchema).parse(result);
    return types;
  }

  static async sourceSpecimenTypes(nameshort: string) {
    const response = await httpClient({
      method: 'GET',
      path: ['studies', nameshort, 'source-specimen-types'],
      body: undefined,
      query: undefined,
    });
    const result = await response.json();
    const types = z.array(sourceSpecimenTypeSchema).parse(result);
    return types;
  }
}
