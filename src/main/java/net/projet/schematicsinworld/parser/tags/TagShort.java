package net.projet.schematicsinworld.parser.tags;

import net.projet.schematicsinworld.parser.utils.BytesStream;
import net.projet.schematicsinworld.parser.utils.ParserException;

import java.nio.ByteBuffer;

public class TagShort extends Tag {

    public TagShort(BytesStream buffer) {
        if (buffer == null) {
            throw new AssertionError("buffer is null");
        }
        this.parseBuffer(buffer);
    }

    public TagShort() {
        // Ne fait rien.
    }

    @Override
    protected void parseBuffer(BytesStream buffer) {
        // Lecture de la clé
        super.setKey(buffer);
        // Lecture de la valeur associée (2 car Tag_SHORT)
        byte[] b = buffer.read(2);
        ByteBuffer wrapped = ByteBuffer.wrap(b);
        this.value = wrapped.getShort();
    }

    @Override
    protected void renderBuffer(BytesStream buffer) throws ParserException {

    }
}
