package net.projet.schematicsinworld.parser.tags;

import net.projet.schematicsinworld.parser.utils.BytesStream;
import net.projet.schematicsinworld.parser.utils.ParserException;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public abstract class Tag implements ITag {

    /*
     * Attributs
     */

    protected String key;
    protected Object value;

    /*
     * Requêtes
     */

    @Override
    public String getKey() {
        return this.key;
    }

    @Override
    public Object getValue() {
        return this.value;
    }

    /*
     * Commandes
     */

    /**
     * Lit le bon nombre de données sur la chaîne contenu dans buffer afin de
     * définir les différentes données du tag.
     *
     * @param buffer Le buffer contenant les prochaines données à lire.
     */
    protected abstract void parseBuffer(BytesStream buffer) throws ParserException;

    protected abstract void renderBuffer(BytesStream buffer) throws ParserException;

    /**
     * Lit 2 + n octets sur buffer :
     * - 2 octets représentant la longueur de la clé.
     * - n octets représentant les n caractères du nom.
     *
     * @param buffer Le buffer.
     */
    protected void setKey(BytesStream buffer) {
        if (buffer == null) {
            throw new AssertionError("Le buffer ne doit pas être nul");
        }
        if (this.key != null) {
            return;
        }
        // 2 octets contenant la longueur du nom
        byte[] b = buffer.read(2);
        // Récupère la clé en convertissant les 2 octets obtenus en un short
        b = buffer.read(ByteBuffer.wrap(b).getShort());
        this.key = new String(b);
    }

    /**
     * Met à jour manuellement la clé d'un tag.
     *
     * @param key La nouvelle clé
     */
    protected void setKey(String key) {
        if (key == null) {
            throw new AssertionError("La clé ne doit pas être nulle");
        }
        this.key = key;
    }

    public void setValue(Object value) throws ParserException {
        if (value == null) {
            throw new ParserException("La valeur ne doit pas être nulle");
        }
        this.value = value;
    }

    protected void renderKey(BytesStream buffer) throws ParserException {
        byte[] len;
        if (this.key.length() > Math.pow(16, 4) - 1) {
            throw new ParserException("La longueur de la clé " + this.key
                    + " est trop grande");
        } else if (this.key.length() > 16 * 16 - 1) {
            len = BigInteger.valueOf(this.key.length()).toByteArray();
        } else {
            len = new byte[] {0, (byte) this.key.length()};
        }
        buffer.write(len);
        buffer.write(this.key.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String toString() {
        return "\nTag{\n" +
                "\tkey='" + key + "',\n" +
                "\tvalue=" + value + "\n" +
                "}";
    }

}
