/*******************************************************************************
 * MIT License
 * 
 * Copyright (c) 2018 Antonin Delpeuch
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 ******************************************************************************/
package org.openrefine.wikidata.qa.scrutinizers;

import org.openrefine.wikidata.qa.ConstraintFetcher;
import org.openrefine.wikidata.testing.TestingData;
import org.openrefine.wikidata.updates.ItemUpdate;
import org.openrefine.wikidata.updates.ItemUpdateBuilder;
import org.testng.annotations.Test;
import org.wikidata.wdtk.datamodel.helpers.Datamodel;
import org.wikidata.wdtk.datamodel.implementation.StatementImpl;
import org.wikidata.wdtk.datamodel.interfaces.ItemIdValue;
import org.wikidata.wdtk.datamodel.interfaces.PropertyIdValue;
import org.wikidata.wdtk.datamodel.interfaces.Snak;
import org.wikidata.wdtk.datamodel.interfaces.SnakGroup;
import org.wikidata.wdtk.datamodel.interfaces.Statement;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.openrefine.wikidata.qa.scrutinizers.InverseConstraintScrutinizer.INVERSE_CONSTRAINT_QID;
import static org.openrefine.wikidata.qa.scrutinizers.InverseConstraintScrutinizer.INVERSE_PROPERTY_PID;
import static org.openrefine.wikidata.qa.scrutinizers.InverseConstraintScrutinizer.SYMMETRIC_CONSTRAINT_QID;

public class InverseConstaintScrutinizerTest extends StatementScrutinizerTest {

    public static PropertyIdValue propertyId = Datamodel.makeWikidataPropertyIdValue("P25");
    public static ItemIdValue propertyValue = Datamodel.makeWikidataItemIdValue("Q345");
    public static PropertyIdValue inversePropertyID = Datamodel.makeWikidataPropertyIdValue("P40");
    public static PropertyIdValue symmetricPropertyID = Datamodel.makeWikidataPropertyIdValue("P3373");
    public static ItemIdValue symmetricPropertyValue = Datamodel.makeWikidataItemIdValue("Q9545711");

    public static ItemIdValue inverseEntityIdValue = Datamodel.makeWikidataItemIdValue(INVERSE_CONSTRAINT_QID);
    public static ItemIdValue symmetricEntityIdValue = Datamodel.makeWikidataItemIdValue(SYMMETRIC_CONSTRAINT_QID);
    public static PropertyIdValue propertyParameter = Datamodel.makeWikidataPropertyIdValue(INVERSE_PROPERTY_PID);

    @Override
    public EditScrutinizer getScrutinizer() {
        return new InverseConstraintScrutinizer();
    }

    @Test
    public void testTrigger() {
        ItemIdValue idA = TestingData.existingId;
        Snak mainSnak = Datamodel.makeValueSnak(propertyId, propertyValue);
        Statement statement = new StatementImpl("P25", mainSnak, idA);
        ItemUpdate update = new ItemUpdateBuilder(idA).addStatement(statement).build();

        Snak qualifierSnak = Datamodel.makeValueSnak(propertyParameter, inverseEntityIdValue);
        List<Snak> qualifierSnakList = Collections.singletonList(qualifierSnak);
        SnakGroup qualifierSnakGroup = Datamodel.makeSnakGroup(qualifierSnakList);
        List<SnakGroup> snakGroupList = Collections.singletonList(qualifierSnakGroup);
        List<Statement> statementList = constraintParameterStatementList(inverseEntityIdValue, snakGroupList);

        ConstraintFetcher fetcher = mock(ConstraintFetcher.class);
        when(fetcher.getConstraintsByType(propertyId, INVERSE_CONSTRAINT_QID)).thenReturn(statementList);
        when(fetcher.findValues(snakGroupList, INVERSE_PROPERTY_PID)).thenReturn(Collections.singletonList(inversePropertyID));
        setFetcher(fetcher);
        scrutinize(update);
        assertWarningsRaised(InverseConstraintScrutinizer.type);
    }

    @Test
    public void testSymmetric() {
        ItemIdValue idA = TestingData.existingId;
        Snak mainSnak = Datamodel.makeValueSnak(symmetricPropertyID, symmetricPropertyValue);
        Statement statement = new StatementImpl("P3373", mainSnak, idA);
        ItemUpdate update = new ItemUpdateBuilder(idA).addStatement(statement).build();

        Snak qualifierSnak = Datamodel.makeValueSnak(symmetricPropertyID, symmetricEntityIdValue);
        List<Snak> qualifierSnakList = Collections.singletonList(qualifierSnak);
        SnakGroup qualifierSnakGroup = Datamodel.makeSnakGroup(qualifierSnakList);
        List<SnakGroup> snakGroupList = Collections.singletonList(qualifierSnakGroup);
        List<Statement> statementList = constraintParameterStatementList(symmetricEntityIdValue, snakGroupList);

        ConstraintFetcher fetcher = mock(ConstraintFetcher.class);
        when(fetcher.getConstraintsByType(symmetricPropertyID, SYMMETRIC_CONSTRAINT_QID)).thenReturn(statementList);
        when(fetcher.findValues(snakGroupList, INVERSE_PROPERTY_PID)).thenReturn(Collections.singletonList(symmetricPropertyID));
        setFetcher(fetcher);
        scrutinize(update);
        assertWarningsRaised(InverseConstraintScrutinizer.type);
    }

    @Test
    public void testNoSymmetricClosure() {
        ItemIdValue idA = TestingData.existingId;
        Snak mainSnak = Datamodel.makeSomeValueSnak(propertyId);
        Statement statement = new StatementImpl("P25", mainSnak, idA);
        ItemUpdate update = new ItemUpdateBuilder(idA).addStatement(statement).build();

        Snak qualifierSnak = Datamodel.makeValueSnak(propertyParameter, inverseEntityIdValue);
        List<Snak> qualifierSnakList = Collections.singletonList(qualifierSnak);
        SnakGroup qualifierSnakGroup = Datamodel.makeSnakGroup(qualifierSnakList);
        List<SnakGroup> snakGroupList = Collections.singletonList(qualifierSnakGroup);
        List<Statement> statementList = constraintParameterStatementList(inverseEntityIdValue, snakGroupList);

        ConstraintFetcher fetcher = mock(ConstraintFetcher.class);
        when(fetcher.getConstraintsByType(propertyId, INVERSE_CONSTRAINT_QID)).thenReturn(statementList);
        when(fetcher.findValues(snakGroupList, INVERSE_PROPERTY_PID)).thenReturn(Collections.singletonList(inversePropertyID));
        setFetcher(fetcher);
        scrutinize(update);
        assertNoWarningRaised();
    }

}
