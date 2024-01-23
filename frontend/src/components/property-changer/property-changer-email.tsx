import { zodResolver } from '@hookform/resolvers/zod';
import { useForm } from 'react-hook-form';
import { z } from 'zod';
import { LabelledInput } from '../forms/labelled-input';
import { PropertyChangerProps } from './property-changer';
import { PropertyChangerDialog } from './property-changer-dialog';

const requiredSchema = z.object({
  value: z.string().trim().email()
});

const optionalSchema = z.object({
  value: z.union([z.literal(''), z.string().trim().email()])
});

export const PropertyChangerEmail: React.FC<PropertyChangerProps<string>> = ({
  propertyName,
  title,
  label,
  value,
  required,
  open,
  onClose
}) => {
  const schema = required ? requiredSchema : optionalSchema;

  const {
    register,
    getValues,
    formState: { isValid, errors }
  } = useForm<z.infer<typeof schema>>({
    mode: 'all',
    reValidateMode: 'onChange',
    resolver: zodResolver(schema),
    defaultValues: { value }
  });

  const handleSubmit = (event: React.SyntheticEvent) => {
    event.preventDefault();
    handleOk();
  };

  const handleOk = () => {
    const values = getValues();
    const value = values?.value;
    onClose('ok', propertyName, value === '' ? null : value);
  };

  const handleCancel = () => {
    onClose('cancel', propertyName, null);
  };

  return (
    <PropertyChangerDialog
      title={title}
      required={required}
      open={open}
      size="md"
      onOk={handleOk}
      onCancel={handleCancel}
      valid={isValid}
    >
      <form onSubmit={handleSubmit}>
        <div className="grid grid-cols-1 gap-6">
          <LabelledInput type="email" label={label} errorMessage={errors?.value?.message} {...register('value')} />
        </div>
      </form>
    </PropertyChangerDialog>
  );
};
