package net.projet.schematicsinworld.parser.tags;

import net.projet.schematicsinworld.parser.utils.BytesStream;
import net.projet.schematicsinworld.parser.utils.ParserException;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class TagLong extends Tag {

    public TagLong(BytesStream buffer) {
        if (buffer == null) {
            throw new AssertionError("buffer is null");
        }
        this.parseBuffer(buffer);
    }

    public TagLong() {
        // Ne fait rien.
    }

    @Override
    protected void parseBuffer(BytesStream buffer) {
        // Lecture de la clé
        super.setKey(buffer);
        // Lecture de la valeur associée (8 car Tag_LONG)
        byte[] b = buffer.read(8);
        ByteBuffer wrapped = ByteBuffer.wrap(b);
        this.value = wrapped.getLong();
    }

    @Override
    protected void renderBuffer(BytesStream buffer) throws ParserException {
        super.renderKey(buffer);
        try {
            // Convertit la valeur en tableau de byte
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            DataOutputStream dstream = new DataOutputStream(stream);
            dstream.writeLong((long) this.value);
            dstream.flush();
            // Ecrit la valeur
            buffer.write(stream.toByteArray());
        } catch (IOException e) {
            throw new ParserException("Impossible de parser le double "
                    + this.value);
        }
    }
}
