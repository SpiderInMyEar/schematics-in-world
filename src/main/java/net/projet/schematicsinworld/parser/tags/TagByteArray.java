package net.projet.schematicsinworld.parser.tags;

import net.projet.schematicsinworld.parser.utils.BytesStream;
import net.projet.schematicsinworld.parser.utils.ParserException;

public class TagByteArray extends TagArray {

    public TagByteArray(BytesStream buffer) throws ParserException {
        if (buffer == null) {
            throw new AssertionError("buffer is null");
        }
        this.parseBuffer(buffer);
    }

    public TagByteArray() {
        // Ne fait rien.
    }

    @Override
    protected void parseBuffer(BytesStream buffer) throws ParserException {
        super.setKey(buffer);
        byte[] b = buffer.read(this.getNbElems(buffer));
        this.value = b;
    }

    @Override
    protected void renderBuffer(BytesStream buffer) throws ParserException {

    }
}
