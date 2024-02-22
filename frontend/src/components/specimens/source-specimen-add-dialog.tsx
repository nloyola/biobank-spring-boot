import { useStudySourceSpecimenTypes } from '@app/hooks/use-study';
import { Status } from '@app/models/status';

import { faVial } from '@fortawesome/free-solid-svg-icons';
import { zodResolver } from '@hookform/resolvers/zod';
import { useForm } from 'react-hook-form';
import { z } from 'zod';

import { CircularProgress } from '../circular-progress';
import { EntityAddDialog } from '../entity-add-dialog';
import { FormLabel } from '../forms/form-label';
import { LabelledInput } from '../forms/labelled-input';
import { SourceSpecimenTypeSelect } from '../forms/source-specimen-type-select';
import { StatusSelect } from '../forms/status-select';
import { ShowError } from '../show-error';

const schema = z.object({
  inventoryId: z.string().min(1, { message: 'an ID is required' }),
  typeNameShort: z.string(),
  timeDrawn: z.string().min(1, { message: 'a time is required' }),
  status: z.nativeEnum(Status),
  comment: z.string().optional(),
});

export type FormInputs = z.infer<typeof schema>;

export const SourceSpecimenAddDialog: React.FC<{
  studyNameShort: string;
  title?: string;
  onSubmit: (newComment: string) => void;
}> = ({ studyNameShort, title, onSubmit }) => {
  const query = useStudySourceSpecimenTypes(studyNameShort);

  const now = new Date();
  now.setUTCHours(0, 0, 0, 0);

  const {
    control,
    register,
    getValues,
    reset,
    formState: { isValid, errors },
  } = useForm<FormInputs>({
    mode: 'all',
    reValidateMode: 'onChange',
    resolver: zodResolver(schema),
    defaultValues: {
      typeNameShort: '',
      status: Status.ACTIVE,
      timeDrawn: now.toISOString().substring(0, 16),
    },
  });

  const handleSubmit = () => {
    const values = getValues();
    onSubmit(values.typeNameShort);
    reset();
  };

  const handleCancel = () => {
    reset();
  };

  return (
    <EntityAddDialog
      title={title ?? 'Entity'}
      message="Add a source specimen"
      buttonLabel="Add Specimen"
      buttonIcon={faVial}
      okButtonEnabled={isValid}
      onOk={handleSubmit}
      onCancel={handleCancel}
    >
      <form>
        <div className="grid grid-cols-1 gap-6">
          {query.isError ? (
            <ShowError error={query.error} />
          ) : (
            <>
              <LabelledInput
                id="inventoryId"
                label="Inventory ID"
                errorMessage={errors?.inventoryId?.message}
                {...register('inventoryId')}
              />

              {query.isLoading || !query.sourceSpecimenTypes ? (
                <CircularProgress />
              ) : (
                <SourceSpecimenTypeSelect
                  control={control}
                  name="typeNameShort"
                  specimenTypes={query.sourceSpecimenTypes}
                />
              )}

              <LabelledInput
                type="datetime-local"
                label="Time Drawn"
                errorMessage={errors?.timeDrawn?.message}
                {...register('timeDrawn')}
                required
              />

              <StatusSelect control={control} name="status" />

              <FormLabel>Comment</FormLabel>
              <textarea
                {...register('comment')}
                className="min-h-[150px] rounded-md"
                placeholder="type your comment here"
              />
              {errors?.comment?.message && (
                <div className="text-sm text-red-600">
                  <span role="alert">{errors?.comment?.message}</span>
                </div>
              )}
            </>
          )}
        </div>
      </form>
    </EntityAddDialog>
  );
};
