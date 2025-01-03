import { ClinicName } from '@app/models/clinic';
import { cn } from '@app/utils';

import { Check, ChevronsUpDown } from 'lucide-react';
import { useState } from 'react';
import { Control, FieldPathByValue, FieldValues, useController } from 'react-hook-form';

import { CommandList } from 'cmdk';
import { Command, CommandEmpty, CommandGroup, CommandInput, CommandItem } from '../ui/command';
import { Popover, PopoverContent, PopoverTrigger } from '../ui/popover';
import { ScrollArea } from '../ui/scroll-area';
import { FormLabel } from './form-label';

const classes =
  'flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background file:border-0 file:bg-transparent file:text-sm file:font-medium placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-offset-2 focus-visible:ring-blue-400 disabled:cursor-not-allowed disabled:opacity-50 border-2 border-slate-400';

export interface ClinicSelectProps<T extends FieldValues, U extends FieldPathByValue<T, string>> {
  control?: Control<T>;
  name: U;
  clinics: ClinicName[];
}

/**
 * Allows the user to select a Clinic.
 *
 * Meant to be used in a react-hook-form.
 */
export function ClinicSelect<T extends FieldValues, U extends FieldPathByValue<T, string>>({
  control,
  name,
  clinics,
}: ClinicSelectProps<T, U>) {
  const { field } = useController({ control, name });
  const [popoverOpen, setPopoverOpen] = useState(false);
  const findClinic = (nameShort: String) => clinics.find((clinic) => clinic.nameShort === nameShort);

  return (
    <div className="grid grid-cols-1 gap-2">
      <FormLabel>Clinic</FormLabel>

      <Popover open={popoverOpen} onOpenChange={setPopoverOpen}>
        <PopoverTrigger>
          <span role="combobox" aria-expanded={popoverOpen} className={cn(classes, 'flex w-full justify-between')}>
            {findClinic(field.value)?.nameShort ?? 'Select a clinic'}
            <ChevronsUpDown className="ml-2 h-4 w-4 shrink-0 opacity-50" />
          </span>
        </PopoverTrigger>
        <PopoverContent className="w-full overflow-y-auto bg-gray-100 p-2" asChild align="start">
          <ScrollArea className="w-full p-2 text-gray-700 md:h-[450px]">
            <Command>
              <CommandInput placeholder="Search..." className="m-1" />
              <CommandList>
                <CommandEmpty className="bg-warning-600 text-basic-100 ml-5 mt-5 rounded-md px-3 py-2">
                  That clinic does not exist
                </CommandEmpty>
                <CommandGroup>
                  {clinics.map((option) => (
                    <CommandItem
                      key={option.id}
                      value={option.nameShort}
                      onSelect={() => {
                        setPopoverOpen(false);
                        field.onChange(option.nameShort);
                      }}
                    >
                      <Check
                        className={cn('mr-2 h-4 w-4', field.value === option.nameShort ? 'opacity-100' : 'opacity-0')}
                      />
                      <div className="flex flex-col items-start">
                        <p>{option.nameShort}</p>
                        <p className="text-sm text-muted-foreground md:max-w-[40rem]">{option.name}</p>
                      </div>
                    </CommandItem>
                  ))}
                </CommandGroup>
              </CommandList>
            </Command>
          </ScrollArea>
        </PopoverContent>
      </Popover>
    </div>
  );
}
