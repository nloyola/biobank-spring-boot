import { zodResolver } from '@hookform/resolvers/zod';
import { format } from 'date-fns';
import { useForm } from 'react-hook-form';
import { z } from 'zod';
import { LabelledInput } from '../forms/labelled-input';
import { PropertyChangerDateRangeProps } from './property-changer';
import { PropertyChangerDialog } from './property-changer-dialog';

const schema = z
  .object({
    startDate: z.string(),
    endDate: z.string().or(z.literal('')) // allows empty string for endDate
  })
  .refine(
    (data) => {
      if (!data.startDate || !data.endDate) {
        return true;
      }
      const startDate = new Date(data.startDate);
      const endDate = new Date(data.endDate);
      return startDate <= endDate;
    },
    { message: 'must be later than start date' }
  );

export function PropertyChangerDateRange({
  propertyName,
  title,
  value,
  required,
  open,
  onClose
}: PropertyChangerDateRangeProps) {
  const {
    register,
    getValues,
    watch,
    formState: { isValid, errors }
  } = useForm<z.infer<typeof schema>>({
    resolver: zodResolver(schema),
    mode: 'all',
    reValidateMode: 'onChange',
    defaultValues: {
      startDate: value?.startDate ? format(value.startDate, 'yyyy-MM-dd') : '',
      endDate: value?.endDate ? format(value.endDate, 'yyyy-MM-dd') : ''
    }
  });

  const watchStartDate = watch('startDate', undefined);
  const watchEndDate = watch('endDate', undefined);

  const handleSubmit = (event: React.SyntheticEvent) => {
    event.preventDefault();
    handleOk();
  };

  const handleOk = () => {
    const values = getValues();
    onClose('ok', propertyName, {
      startDate: new Date(values.startDate),
      endDate: new Date(values.endDate)
    });
  };

  const handleCancel = () => {
    onClose('cancel', propertyName, null);
  };

  return (
    <PropertyChangerDialog
      size="md"
      title={title}
      required={required}
      open={open}
      onOk={handleOk}
      onCancel={handleCancel}
      valid={isValid}
    >
      <form onSubmit={handleSubmit} className="grid-columns-1 grid gap-4">
        <LabelledInput
          type="date"
          label="Start"
          errorMessage={errors?.startDate?.message}
          {...register('startDate')}
          max={watchEndDate}
          pattern="\d{4}-\d{2}-\d{2}"
        />
        <LabelledInput
          type="date"
          label="End"
          errorMessage={errors?.endDate?.message}
          {...register('endDate')}
          min={watchStartDate}
          pattern="\d{4}-\d{2}-\d{2}"
        />
      </form>
    </PropertyChangerDialog>
  );
}
