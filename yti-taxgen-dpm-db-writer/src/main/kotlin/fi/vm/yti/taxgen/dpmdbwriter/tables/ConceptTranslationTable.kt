package fi.vm.yti.taxgen.dpmdbwriter.tables

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

/**
 * Reference DDL (from BR-AG Data Modeler):
 * CREATE TABLE `mConceptTranslation` (
 *   `ConceptID` INTEGER,
 *   `LanguageID` INTEGER,
 *   `Text` TEXT,
 *   `Role` TEXT,
 *   FOREIGN KEY(`ConceptID`) REFERENCES `mConcept`(`ConceptID`),
 *   PRIMARY KEY(`ConceptID`,`LanguageID`,`Role`),
 *   FOREIGN KEY(`LanguageID`) REFERENCES `mLanguage`(`LanguageID`)
 * );
 *
 * Role reference values (from BR-AG Data Modeler):
 * - `label`
 *
 * Entity differences between the reference (BR-AG DM) and Tool for Undertakings (T4U):
 * - T4U defines additional column: OwnerID(INTEGER)
 * - T4U does not define column: Role(TEXT)
 */
object ConceptTranslationTable : Table("mConceptTranslation") {
    val conceptID = reference("ConceptID", ConceptTable, ReferenceOption.NO_ACTION).nullable().primaryKey()
    val languageID = reference("LanguageID", LanguageTable, ReferenceOption.NO_ACTION).nullable().primaryKey()

    val text = text("Text").nullable()
    val role = text("Role").nullable().primaryKey()
}
