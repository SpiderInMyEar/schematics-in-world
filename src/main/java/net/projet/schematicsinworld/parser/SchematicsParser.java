package net.projet.schematicsinworld.parser;

import jdk.nashorn.internal.ir.Block;
import net.minecraft.entity.monster.AbstractRaiderEntity;
import net.projet.schematicsinworld.parser.tags.*;
import net.projet.schematicsinworld.parser.utils.BlockData;
import net.projet.schematicsinworld.parser.utils.ParserException;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Set;

/**
 * Convertit des fichiers .schem générés par WorldEdit en fichier .nbt
 * générés par des structures blocs.
 */
public class SchematicsParser {

    /*
     * CONSTANTES
     */

    public final static String BLOCKS = "BlockData";
    public final static String PALETTE = "Palette";

    /*
     * ATTRIBUTS
     */

    // Les tags parsés dans le fichier passé dans le constructeur
    private ArrayList<Tag> tags = null;
    // Le fichier schematic source
    private final File file;

    /*
     * CONSTRUCTEURS
     */

    public SchematicsParser(String filepath) {
        if (filepath == null) {
            throw new AssertionError("filepath is null");
        }
        file = new File(filepath);
        if (!file.isFile() || !file.canRead()) {
            throw new AssertionError("cannot open file");
        }

        try {
            NBTParser nbtp = new NBTParser(filepath);
            this.tags = nbtp.getTags();
        } catch (ParserException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }

    /*
     * REQUETES
     */

    public File getFile() {
        return file;
    }

    /*
     * METHODES
     */

    public void parseBlocks() {
        for (Tag t : tags) {
            if (t.getKey() == BLOCKS) {
                Object[] values = (Object[]) t.getValue();
            }
        }
    }

    public void saveToNBT(String filepath) throws ParserException {
        // Convertit le fichier .schem en NBT de structure bloc
        ArrayList<Tag> tags = this.convertSchematicsToNBT();
        // Enregistre le fichier
        try {
            new NBTParser(filepath, 'w', tags);
        } catch (ParserException e) {
            e.printStackTrace();
        }
    }

    /*
     * Outils
     */

    @SuppressWarnings("unchecked")
    private ArrayList<Tag> convertSchematicsToNBT() throws ParserException {
        ArrayList<Tag> res = new ArrayList<>();
        ArrayList<Tag> size = new ArrayList<>();
        for (int i = 0; i < 3; ++i) { size.add(new TagInt()); }
        byte[] blocks = new byte[] {};
        ArrayList<TagCompound> blockEntities = null;
        for (Tag t : this.tags) {
            switch (t.getKey()) {
                case "DataVersion":
                    TagInt dataVersion = new TagInt();
                    dataVersion.setKey("DataVersion");
                    dataVersion.setValue(t.getValue());
                    res.add(dataVersion);
                    break;
                case "Palette":
                    this.convertPalette(res, (TagCompound) t);
                    break;
                case "Length":
                    size.get(0).setValue(((Short) t.getValue()).intValue());
                    break;
                case "Height":
                    size.get(1).setValue(((Short) t.getValue()).intValue());
                    break;
                case "Width":
                    size.get(2).setValue(((Short) t.getValue()).intValue());
                    break;
                case "BlockData":
                    blocks = (byte[]) t.getValue();
                    break;
                case "BlockEntities":
                    blockEntities = (ArrayList<TagCompound>) t.getValue();
                    break;
            }
        }
        // TODO
        //this.convertEntities(res);

        // blocks
        ArrayList<BlockData> blockData = this.convertBlocks(blocks, blockEntities, size);
        TagList blocksTag = new TagList();
        ArrayList<TagCompound> blocksList = new ArrayList<>();
        for (BlockData bd : blockData) {
            TagCompound tc = new TagCompound();
            ArrayList<Tag> tcList = new ArrayList<>();
            // state
            TagInt state = new TagInt();
            state.setKey("state");
            state.setValue(bd.getState());
            tcList.add(state);
            // pos
            TagList tl = new TagList();
            tl.setKey("pos");
            ArrayList<TagInt> coords = new ArrayList<>();
            for(Integer i : bd.getCoords()) {
                TagInt ti = new TagInt();
                ti.setValue(i);
                coords.add(ti);
            }
            tl.setValue(coords);
            tcList.add(tl);
            // nbt
            if (bd.getNbt().size() > 0) {
                TagCompound nbt = new TagCompound();
                ArrayList<TagString> nbtValues = new ArrayList<>();
                nbt.setKey("nbt");
                for (String key : bd.getNbt().keySet()) {
                    TagString ts = new TagString();
                    ts.setKey(key);
                    ts.setValue(bd.getNbt().get(key));
                }
                nbt.setValue(nbtValues);
                tcList.add(nbt);
            }
            // fin
            tc.setValue(tcList);
            blocksList.add(tc);
        }
        blocksTag.setValue(blocksList);
        res.add(blocksTag);
        // size
        TagList sizeTag = new TagList();
        sizeTag.setKey("size");
        sizeTag.setValue(size);
        res.add(sizeTag);

        return res;
    }

    @SuppressWarnings("unchecked")
    private void convertPalette(ArrayList<Tag> res, TagCompound schemPalette) throws ParserException {
        TagList palette = new TagList();
        ArrayList<Tag> paletteVal = new ArrayList<>();
        // Transforme le dictionnaire schemPalette en TagList de TagCompound
        ((ArrayList<Tag>) schemPalette.getValue()).sort(
            Comparator.comparingInt(o -> ((Integer) o.getValue()))
        );
        for (Tag t : (ArrayList<Tag>) schemPalette.getValue()) {
            // Sépare la clé du NBT et ses propriétés
            String[] key_prop = t.getKey().split("\\[");

            TagCompound tagCompound = new TagCompound();

            ArrayList<Tag> compoundVal = new ArrayList<>();
            TagString compoundValName = new TagString();
            compoundValName.setKey("Name");
            compoundValName.setValue(key_prop[0]);
            compoundVal.add(compoundValName);

            if (key_prop.length > 1) {
                key_prop[1] = key_prop[1].substring(0, key_prop[1].length() - 1);
                TagCompound props = new TagCompound();
                ArrayList<Tag> propsVal = new ArrayList<>();
                for (String prop : key_prop[1].split(",")) {
                    String[] prop_name_val = prop.split("=");
                    TagString propTagString = new TagString();
                    propTagString.setKey(prop_name_val[0]);
                    propTagString.setValue(prop_name_val[1]);
                    propsVal.add(propTagString);
                }
                props.setKey("Properties");
                props.setValue(propsVal);
                compoundVal.add(props);
            }

            tagCompound.setValue(compoundVal);
            paletteVal.add(tagCompound);
        }
        // Ajoute la liste au résultat
        palette.setKey("palette");
        palette.setValue(paletteVal);
        res.add(palette);
    }

    private void convertEntities(ArrayList<Tag> res) {
        TagList entities = new TagList();
        res.add(entities);
    }

    @SuppressWarnings("unchecked")
    private ArrayList<BlockData> convertBlocks(byte[] blockData, ArrayList<TagCompound> blockEntities, ArrayList<Tag> size) {
        TagList blocks = new TagList();
        ArrayList<BlockData> blocksVal = new ArrayList<>();
        int nextX = 0;
        int nextY = 0;
        int nextZ = 0;
        for (byte b : blockData) {
            HashMap<String, String> nbt = new HashMap<>();
            boolean isHere = false;
            for (TagCompound tc : blockEntities) {
                for (Tag t : (ArrayList<Tag>) tc.getValue()) {
                    if (t.getKey().equals("Pos")) {
                        int[] tagPos = (int[]) t.getValue();
                        if (tagPos[0] == nextX && tagPos[1] == nextY && tagPos[2] == nextZ) {
                            isHere = true;
                            continue;
                        }
                        break;
                    }
                    if (t.getKey().equals("Id")) {
                        nbt.put("id", (String) t.getValue());
                    } else {
                        nbt.put(t.getKey(), (String) t.getValue());
                    }
                }
            }
            BlockData bd = new BlockData(nextX, nextY, nextZ, b, isHere ? nbt : new HashMap<>());
            blocksVal.add(bd);
            nextZ = (nextZ + 1) % (int) size.get(2).getValue();
            if (nextZ == 0) {
                nextX = (nextX + 1) % (int) size.get(0).getValue();
                if (nextX == 0) {
                    nextY = (nextY + 1) % (int) size.get(1).getValue();
                }
            }
        }
        return blocksVal;
    }
}
