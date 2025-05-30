//
// Windows API Generator for Java
// Copyright (c) 2025 Manuel Bleichenbacher
// Licensed under MIT License
// https://opensource.org/licenses/MIT
//
package net.codecrete.windowsapi.winmd;

import net.codecrete.windowsapi.metadata.Struct;
import net.codecrete.windowsapi.metadata.Type;

/**
 * Decoder for method signatures.
 */
class SignatureDecoder extends Decoder {
    /**
     * Creates a new decoder.
     *
     * @param typeLookup an instance implementing the lookup of types
     */
    SignatureDecoder(TypeLookup typeLookup) {
        super(typeLookup);
    }

    /**
     * Decodes a {@code MethodDef} signature.
     *
     * @param signature the BLOB containing the signature
     * @return the decoded signature
     */
    MethodSignature decodeMethodDefSignature(Blob signature) {
        // See ECMA-335, II.23.2.1
        var flags = signature.readByte();
        var genericParamCount = 0;
        if ((flags & 0x10) != 0) {
            genericParamCount = signature.readCompressedUnsignedInt();
            assert genericParamCount == 0;
        }

        var paramCount = signature.readCompressedUnsignedInt();
        var returnType = decodeType(signature, null);
        var params = new Type[paramCount];
        for (int i = 0; i < paramCount; i++) {
            params[i] = decodeType(signature, null);
        }
        assert signature.isAtEnd();
        return new MethodSignature(returnType, params);
    }

    /**
     * Decodes a {@code Field} signature.
     *
     * @param signature  the BLOB containing the signature
     * @param parentType the type containing the field
     * @return the decoded signature
     */
    Type decodeFieldSignature(Blob signature, Struct parentType) {
        // See ECMA-335, II.23.2.4 FieldSig
        var field = signature.readByte();
        assert field == 0x06;

        // See CustomMod, ECMA-335, §II.23.2.7
        // ignore

        // See Type, ECMA-335, §II.23.2.12
        var type = decodeType(signature, parentType);
        assert signature.isAtEnd();
        return type;
    }
}
