const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user: cannot read entries", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("entries").get());
});

test("Authenticated user: cannot read another user's entries", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(BOB_UID).collection("entries").doc("entry_1").set({
      userId: BOB_UID,
      dateMillis: 1700000000000,
      label: "Course au supermarché",
      debitAccountCode: "621",
      debitAccountName: "Alimentation",
      creditAccountCode: "531",
      creditAccountName: "Orange Money",
      amount: 15000,
      categoryType: "EXPENSE",
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("users").doc(BOB_UID).collection("entries").doc("entry_1").get());
});

test("Authenticated user: can create, read, and delete their own entry", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const entryRef = aliceDb.collection("users").doc(ALICE_UID).collection("entries").doc("entry_alice_1");

  await assertSucceeds(entryRef.set({
    userId: ALICE_UID,
    dateMillis: 1700000000000,
    reference: "REF-001",
    label: "Salaire Novembre",
    debitAccountCode: "521",
    debitAccountName: "Comptes bancaires",
    creditAccountCode: "701",
    creditAccountName: "Salaire",
    amount: 500000,
    categoryType: "REVENUE",
    note: "Virement employeur"
  }));

  await assertSucceeds(entryRef.get());
  await assertSucceeds(entryRef.delete());
});

test("Spoofing: cannot write entry with mismatched userId", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const entryRef = aliceDb.collection("users").doc(ALICE_UID).collection("entries").doc("entry_spoofed");

  await assertFails(entryRef.set({
    userId: BOB_UID,
    dateMillis: 1700000000000,
    label: "Tentative spoof",
    debitAccountCode: "621",
    debitAccountName: "Alimentation",
    creditAccountCode: "511",
    creditAccountName: "Espèces",
    amount: 5000,
    categoryType: "EXPENSE"
  }));
});
