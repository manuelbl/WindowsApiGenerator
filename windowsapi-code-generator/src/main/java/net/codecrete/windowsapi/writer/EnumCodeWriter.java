//
// Windows API Generator for Java
// Copyright (c) 2025 Manuel Bleichenbacher
// Licensed under MIT License
// https://opensource.org/licenses/MIT
//
package net.codecrete.windowsapi.writer;

import net.codecrete.windowsapi.metadata.EnumType;
import net.codecrete.windowsapi.metadata.Member;

/**
 * Creates the Java code for enumerations.
 */
class EnumCodeWriter extends JavaCodeWriter<EnumType> {

    /**
     * Creates a new instance.
     *
     * @param generationContext the code generation context
     */
    EnumCodeWriter(GenerationContext generationContext) {
        super(generationContext);
    }

    /**
     * Creates a new file with the Java code for the specified enumeration.
     *
     * @param enumeration the enumeration
     */
    void writeEnum(EnumType enumeration) {
        var className = toJavaClassName(enumeration.name());
        withFile(enumeration.namespace(), enumeration, className, this::writeEnumValues);
    }

    private void writeEnumValues() {
        var javaType = getPrimitiveJavaType(type.baseType());
        var fields = type.members();

        var prefix = className + "_";
        var haveCommonPrefix = fields.stream().allMatch(field -> field.name().startsWith(prefix));

        writer.printf("package %s ;%n%n", packageName);

        writeEnumComment(haveCommonPrefix);

        writer.printf("""
                public class %1$s {
                """, className);


        for (Member field : fields) {
            var name = haveCommonPrefix ? field.name().substring(prefix.length()) : field.name();
            if (Character.isDigit(name.charAt(0)))
                name = "_" + name;
            var constantValue = getJavaIntegerConstant(javaType, field.value());
            writeComment("Enumeration value {@code %s}", field.name());
            writer.printf("""
                        public static final %s %s = %s;
                    
                    """, javaType, name, constantValue);
        }

        writer.printf("""
                
                    private %s() {}
                }
                """, className);
    }

    void writeEnumComment(boolean haveCommonPrefix) {
        writer.printf("""
                        /**
                         * {@code %1$s} %2$s
                        """,
                type.nativeName(),
                type.isEnumFlags() ? "enumeration flags" : "enumeration");

        if (haveCommonPrefix) {
            writer.printf("""
                     * <p>
                     * The enumeration member names do not include the prefix {@code %s} as it is the same as the enumeration name.
                     * </p>
                    """, type.nativeName());
        }

        writeDocumentationUrl(type);

        writer.println(" */");
    }
}
