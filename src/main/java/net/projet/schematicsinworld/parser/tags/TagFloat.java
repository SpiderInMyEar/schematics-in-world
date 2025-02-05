package net.projet.schematicsinworld.parser.tags;

import net.projet.schematicsinworld.parser.utils.BytesStream;
import net.projet.schematicsinworld.parser.utils.ParserException;

import java.nio.ByteBuffer;

public class TagFloat extends Tag {

    public TagFloat(BytesStream buffer) {
        if (buffer == null) {
            throw new AssertionError("buffer is null");
        }
        this.parseBuffer(buffer);
    }

    public TagFloat() {
        // Ne fait rien.
    }

    @Override
    protected void parseBuffer(BytesStream buffer) {
        // Lecture de la clé
        super.setKey(buffer);
        // Lecture de la valeur associée (4 car Tag_FLOAT)
        byte[] b = buffer.read(4);
        ByteBuffer wrapped = ByteBuffer.wrap(b);
        this.value = wrapped.getFloat();
    }

    @Override
    protected void renderBuffer(BytesStream buffer) throws ParserException {

    }
}
