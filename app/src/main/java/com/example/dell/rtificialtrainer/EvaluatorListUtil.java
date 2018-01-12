package com.example.dell.rtificialtrainer;

import org.dmg.pmml.PMML;
import org.jpmml.evaluator.Evaluator;
import org.jpmml.evaluator.ModelEvaluator;
import org.jpmml.evaluator.ModelEvaluatorFactory;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by DELL on 2016-03-30.
 */
public class EvaluatorListUtil {

    /**
     * Zwraca listę ewaluatorów utworzoną na podstawie listy modeli PMML
     * @param pmmlList lista modeli PMML
     * @return lista obiektów Evaluator
     * @throws Exception
     */
    public static List<Evaluator> createEvaluatorList(List<PMML> pmmlList)throws Exception{

        List<Evaluator> evaluatorList = new ArrayList<>();

        for(PMML pmml: pmmlList){
            ModelEvaluatorFactory modelEvaluatorFactory = ModelEvaluatorFactory.newInstance();

            ModelEvaluator<?> modelEvaluator = modelEvaluatorFactory.newModelManager(pmml);
            modelEvaluator.verify();

            evaluatorList.add(modelEvaluator);
        }

        return evaluatorList;
    }

    /**
     * Zwraca zrzutowany obiekt Map
     * @param is InputStream odnoszący się do pliku modelu
     * @return Map z obiektami List PMML oraz List GTChart zawarty w pliku modelu
     * @throws ClassNotFoundException
     * @throws IOException
     */
    static public Map<String,List> deserializeMapPMML(InputStream is) throws ClassNotFoundException, IOException {
        return (Map<String,List>)deserializeMap(is);
    }

    /**
     * Deserializuje obiekt Map zawarty w pliku modelu
     * @param is InputStream pliku modelu
     * @return Object zawierający dane o modelu
     * @throws ClassNotFoundException
     * @throws IOException
     */
    static private Object deserializeMap(InputStream is) throws ClassNotFoundException, IOException {
        FilterInputStream safeIs = new FilterInputStream(is){

            @Override
            public void close(){
            }
        };

        try{
            ObjectInputStream ois = new ObjectInputStream(safeIs);
            return ois.readObject();
        }catch(IOException e){
            throw e;
        }catch (ClassNotFoundException cnfe){
            throw cnfe;
        }
    }
}
