package net.projet.schematicsinworld.parser;

import net.projet.schematicsinworld.parser.tags.Tag;
import net.projet.schematicsinworld.parser.tags.TagCompound;
import net.projet.schematicsinworld.parser.tags.Tags;
import net.projet.schematicsinworld.parser.utils.BytesStream;
import net.projet.schematicsinworld.parser.utils.ParserException;

import java.io.*;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;

class NBTParser extends TagCompound {

    /*
     * Attributs
     */

    // Flux d'octets qui sera lu lors du parsing.
    private BytesStream buffer;

    /*
     * Constructeur
     */

    public NBTParser(String filepath, char mode, ArrayList<Tag> tags) throws ParserException {
        super();
        if (mode == 'r') {
            // Initialisation du buffer en mode lecture
            this.buffer = new BytesStream(BytesStream.READ_MODE);
            // Décompresse le fichier et stock ses données dans buffer.
            this.extractFile(filepath);
            // Parse le fichier
            this.parseBuffer();
        } else {
            // Initialisation du buffer en mode écriture
            this.buffer = new BytesStream(BytesStream.WRITE_MODE);
            // Dans le cas où l'utilisateur souhaite écrire un fichier NBT,
            // on prend ses tags passés en paramètres afin de créer le fichier
            if (tags == null) {
                throw new ParserException("Les tags fournis sont nuls");
            }
            this.setKey("NBT");
            this.setValue(tags);
            // Parse les tags et les écrit dans le buffer
            super.renderBuffer(this.buffer);
            // Création du fichier
            /*
            try {
                FileOutputStream output = new FileOutputStream(filepath);
                output.write(this.buffer.getContent());
            } catch (IOException e) {
                e.printStackTrace();
            }*/
            // Compresse le fichier
        }
    }

    public NBTParser(String filepath) throws ParserException {
        this(filepath, 'r', null);
    }

    /*
     * Requêtes
     */

    public ArrayList<Tag> getTags() {
        return new ArrayList<Tag>((ArrayList<Tag>) this.getValue());
    }

    /*
     * Commandes
     */

    private void extractFile(String filepath) throws ParserException {
        // Décompression du fichier
        byte[] fileContent;
        try {
            // Parcours le fichier en le décompressant
            FileInputStream fis = new FileInputStream(filepath);
            GZIPInputStream gis = new GZIPInputStream(fis);
            ArrayList<Byte> bufferList = new ArrayList<Byte>();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = gis.read(buffer)) != -1) {
                for (int i = 0; i < len; ++i) {
                    bufferList.add(buffer[i]);
                }
            }
            // Remplit un tableau contenant l'intégralité du fichier
            fileContent = new byte[bufferList.size()];
            int i = 0;
            for (Byte b : bufferList) {
                fileContent[i] = b;
                ++i;
            }
            gis.close();
            fis.close();
        } catch (FileNotFoundException err) {
            throw new ParserException(
                "Une erreur est survenue lors de l'ouverture du fichier"
            );
        } catch (IOException e) {
            throw new ParserException(
                "Une erreur est survenue lors de la décompression du fichier"
            );
        }
        // Stocke les octets lu dans le flux d'octets
        this.buffer.setBytes(fileContent);
    }

    protected void parseBuffer() throws ParserException {
        // Lit le premier octet
        if (this.buffer.read(1)[0] != Tags.TAG_COMPOUND.ordinal()) {
            // Si celui-ci est différent de TAG_COMPOUND on renvoie une
            // exception
            throw new ParserException("Le fichier NBT est invalide");
        }
        // Parsing du reste du fichier
        super.parseBuffer(this.buffer);
    }

}
