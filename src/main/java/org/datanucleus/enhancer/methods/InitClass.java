/**********************************************************************
Copyright (c) 2007 Andy Jefferson and others. All rights reserved.
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
package org.datanucleus.enhancer.methods;

import org.datanucleus.enhancer.ClassEnhancer;
import org.datanucleus.enhancer.ClassMethod;
import org.datanucleus.enhancer.asm.MethodVisitor;
import org.datanucleus.enhancer.asm.Opcodes;

/**
 * Method to generate a static initialisation block for the class using ASM.
 * <pre>
 * static
 * {
 *     dnFieldNames = __dnFieldNamesInit();
 *     dnFieldTypes = __dnFieldTypesInit();
 *     dnFieldFlags = __dnFieldFlagsInit();
 *     dnInheritedFieldCount = __dnGetInheritedFieldCount();
 *     dnPersistableSuperclass = __dnPersistableSuperclassInit();
 *     EnhancementHelper.registerClass(___dn$loadClass("mydomain.MyClass"),
 *         dnFieldNames, dnFieldTypes, dnFieldFlags, dnPersistableSuperclass, new MyClass());
 * }
 * </pre>
 */
public class InitClass extends ClassMethod
{
    public static InitClass getInstance(ClassEnhancer enhancer)
    {
        return new InitClass(enhancer, "<clinit>",
            Opcodes.ACC_STATIC,
            null, null, null);
    }

    /**
     * Constructor.
     * @param enhancer ClassEnhancer
     * @param name Name of method
     * @param access Access type
     * @param returnType Return type
     * @param argTypes Argument types
     * @param argNames Argument names
     */
    public InitClass(ClassEnhancer enhancer, String name, int access, Object returnType, Object[] argTypes, String[] argNames)
    {
        super(enhancer, name, access, returnType, argTypes, argNames);
    }

    /**
     * Method to add the contents of the class method.
     */
    public void execute()
    {
        visitor.visitCode();

        addInitialiseInstructions(visitor);

        visitor.visitInsn(Opcodes.RETURN);
        visitor.visitMaxs(7, 0);

        visitor.visitEnd();
    }

    /**
     * Convenience method to add the initialise instructions to the supplied MethodVisitor.
     * Available as a separate method so that the initialise instructions can be added to an existing static class initialise block (where the class already has one).
     * @param mv MethodVisitor to use
     */
    public void addInitialiseInstructions(MethodVisitor mv)
    {
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, getClassEnhancer().getASMClassName(), getNamer().getFieldNamesInitMethodName(), "()[Ljava/lang/String;");
        mv.visitFieldInsn(Opcodes.PUTSTATIC, getClassEnhancer().getASMClassName(), getNamer().getFieldNamesFieldName(), "[Ljava/lang/String;");

        // TODO Drop this block when we remove "dnFieldTypes" from the enhancement contract.
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, getClassEnhancer().getASMClassName(), getNamer().getFieldTypesInitMethodName(), "()[Ljava/lang/Class;");
        mv.visitFieldInsn(Opcodes.PUTSTATIC, getClassEnhancer().getASMClassName(), getNamer().getFieldTypesFieldName(), "[Ljava/lang/Class;");

        // TODO Drop this block when we remove "dnFieldFlags" from the enhancement contract.
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, getClassEnhancer().getASMClassName(), getNamer().getFieldFlagsInitMethodName(), "()[B");
        mv.visitFieldInsn(Opcodes.PUTSTATIC, getClassEnhancer().getASMClassName(), getNamer().getFieldFlagsFieldName(), "[B");

        mv.visitMethodInsn(Opcodes.INVOKESTATIC, getClassEnhancer().getASMClassName(), getNamer().getGetInheritedFieldCountMethodName(), "()I");
        mv.visitFieldInsn(Opcodes.PUTSTATIC, getClassEnhancer().getASMClassName(), getNamer().getInheritedFieldCountFieldName(), "I");

        // TODO Change this to just call the method when we remove "dnPersistableSuperclass" from the enhancement contract.
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, getClassEnhancer().getASMClassName(), getNamer().getPersistableSuperclassInitMethodName(), "()Ljava/lang/Class;");
        mv.visitFieldInsn(Opcodes.PUTSTATIC, getClassEnhancer().getASMClassName(), getNamer().getPersistableSuperclassFieldName(), "Ljava/lang/Class;");

        mv.visitLdcInsn(getClassEnhancer().getClassName());
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, getClassEnhancer().getASMClassName(), getNamer().getLoadClassMethodName(), "(Ljava/lang/String;)Ljava/lang/Class;");

        mv.visitFieldInsn(Opcodes.GETSTATIC, getClassEnhancer().getASMClassName(), getNamer().getFieldNamesFieldName(), "[Ljava/lang/String;");
        mv.visitFieldInsn(Opcodes.GETSTATIC, getClassEnhancer().getASMClassName(), getNamer().getFieldTypesFieldName(), "[Ljava/lang/Class;");
        mv.visitFieldInsn(Opcodes.GETSTATIC, getClassEnhancer().getASMClassName(), getNamer().getFieldFlagsFieldName(), "[B");
        mv.visitFieldInsn(Opcodes.GETSTATIC, getClassEnhancer().getASMClassName(), getNamer().getPersistableSuperclassFieldName(), "Ljava/lang/Class;");

        if (enhancer.getClassMetaData().isAbstract())
        {
            mv.visitInsn(Opcodes.ACONST_NULL);
        }
        else
        {
            mv.visitTypeInsn(Opcodes.NEW, getClassEnhancer().getASMClassName());
            mv.visitInsn(Opcodes.DUP);
            mv.visitMethodInsn(Opcodes.INVOKESPECIAL, getClassEnhancer().getASMClassName(), "<init>", "()V");
        }
        // TODO Skip the 3 field property args and the superclass arg
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, getNamer().getImplHelperAsmClassName(), "registerClass",
            "(Ljava/lang/Class;" +
            "[Ljava/lang/String;" +
            "[Ljava/lang/Class;" +
            "[BLjava/lang/Class;" +
            "L" + getNamer().getPersistableAsmClassName() + ";)V");
    }
/*
THIS IS HOW THE CODE SHOULD LOOK WHEN WE GET RID OF dnFieldFlags, dnFieldTypes, dnPersistableSuperclass

mv = cw.visitMethod(ACC_STATIC, "<clinit>", "()V", null, null);
mv.visitCode();

---
mv.visitMethodInsn(INVOKESTATIC, "mydomain/model/Base", "__dnFieldNamesInit", "()[Ljava/lang/String;", false);
mv.visitFieldInsn(PUTSTATIC, "mydomain/model/Base", "dnFieldNames", "[Ljava/lang/String;");

mv.visitMethodInsn(INVOKESTATIC, "mydomain/model/Base", "__dnGetInheritedFieldCount", "()I", false);
mv.visitFieldInsn(PUTSTATIC, "mydomain/model/Base", "dnInheritedFieldCount", "I");

mv.visitMethodInsn(INVOKESTATIC, "mydomain/model/Base", "__dnPersistableSuperclassInit", "()Ljava/lang/Class;", false);
mv.visitInsn(POP);

mv.visitLdcInsn("mydomain.model.Base");
mv.visitMethodInsn(INVOKESTATIC, "mydomain/model/Base", "___dn$loadClass", "(Ljava/lang/String;)Ljava/lang/Class;", false);

mv.visitTypeInsn(NEW, "mydomain/model/Base");
mv.visitInsn(DUP);
mv.visitMethodInsn(INVOKESPECIAL, "mydomain/model/Base", "<init>", "()V", false);

mv.visitMethodInsn(INVOKESTATIC, "org/datanucleus/enhancer/EnhancementHelper", "registerClass", "(Ljava/lang/Class;Lorg/datanucleus/enhancement/Persistable;)V", false);
---

mv.visitInsn(RETURN);
mv.visitMaxs(3, 0);
 */
}