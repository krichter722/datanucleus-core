/**********************************************************************
Copyright (c) 2014 Andy Jefferson and others. All rights reserved.
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

Contributors:
    ...
**********************************************************************/
package org.datanucleus.store.types.converters;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * Class to handle the conversion between java.util.Calendar and (Long, String) being the time in millisecs plus the timezone.
 */
public class CalendarComponentsConverter implements TypeConverter<Calendar, Object[]>, MultiColumnConverter
{
    private static final long serialVersionUID = 4725781321319221471L;

    public Calendar toMemberType(Object[] ts)
    {
        if (ts == null)
        {
        	return null;
        }

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis((Long)ts[0]);
        cal.setTimeZone(TimeZone.getTimeZone((String)ts[1]));
        return cal;
    }

    public Object[] toDatastoreType(Calendar cal)
    {
        if (cal == null)
        {
            return null;
        }
        return new Object[] {cal.getTimeInMillis(), cal.getTimeZone().getID()};
    }

    /* (non-Javadoc)
     * @see org.datanucleus.store.types.converters.MultiColumnConverter#getDatastoreColumnTypes()
     */
    @Override
    public Class[] getDatastoreColumnTypes()
    {
        return new Class[] {Long.class, String.class};
    }
}