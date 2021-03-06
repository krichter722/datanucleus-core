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

/**
 * Class to handle the conversion between Character and a String form.
 */
public class CharacterStringConverter implements TypeConverter<Character, String>
{
    private static final long serialVersionUID = 5510626063899761384L;

    public Character toMemberType(String str)
    {
        if (str == null)
        {
            return null;
        }

        return str.charAt(0);
    }

    public String toDatastoreType(Character chr)
    {
        return chr != null ? "" + chr : null;
    }
}