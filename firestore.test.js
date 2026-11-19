const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_uid_123";
const BOB_UID = "bob_uid_456";

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

function buildValidProfile(uid, ts) {
  return {
    userId: uid,
    displayName: "Soufiane",
    language: "ar",
    themeMode: "system",
    cityName: "Casablanca",
    latitude: 33.5731,
    longitude: -7.5898,
    calculationMethod: "MOROCCO",
    quranFontSize: 24,
    lastSurahNumber: 1,
    lastAyahNumber: 1,
    lastSurahName: "الفاتحة",
    tasbihTotalCount: 33,
    createdAt: ts,
    updatedAt: ts,
  };
}

test("Unauthenticated user cannot read or write user profiles, bookmarks, or adhkar progress", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  const ts = new Date(Date.now() - 2000);

  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).set(buildValidProfile(ALICE_UID, ts)));
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("bookmarks").get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("adhkar_progress").get());
});

test("Authenticated owner can create, read, and partially update own profile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const created = new Date(Date.now() - 10000);

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set(buildValidProfile(ALICE_UID, created))
  );
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).get());

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      lastSurahNumber: 18,
      lastAyahNumber: 10,
      lastSurahName: "الكهف",
      tasbihTotalCount: 99,
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Reject immutable createdAt mutation
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      createdAt: new Date(Date.now() - 500),
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Reject ghost field / invalid calculationMethod
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      isAdmin: true,
      updatedAt: new Date(Date.now() - 500),
    })
  );
});

test("Cross-user access to profile, bookmarks, and adhkar progress is denied", async () => {
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const ts = new Date(Date.now() - 5000);

  await assertSucceeds(bobDb.collection("users").doc(BOB_UID).set(buildValidProfile(BOB_UID, ts)));
  await assertSucceeds(
    bobDb.collection("users").doc(BOB_UID).collection("bookmarks").doc("quran_2_255").set({
      userId: BOB_UID,
      itemType: "QURAN",
      title: "سورة البقرة - آية 255",
      subtitle: "Ayat al-Kursi",
      arabicText: "اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ",
      referenceKey: "quran_2_255",
      createdAt: ts,
      updatedAt: ts,
    })
  );

  await assertFails(aliceDb.collection("users").doc(BOB_UID).get());
  await assertFails(aliceDb.collection("users").doc(BOB_UID).collection("bookmarks").get());
  await assertFails(
    aliceDb.collection("users").doc(BOB_UID).collection("bookmarks").doc("quran_2_255").delete()
  );
});

test("Owner can manage bookmarks and adhkar_progress without @DocumentId id field", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const ts = new Date(Date.now() - 5000);

  // List on empty subcollection succeeds
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).collection("bookmarks").get());
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).collection("adhkar_progress").get());

  // Valid bookmark create (no id key)
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("bookmarks").doc("quran_1_1").set({
      userId: ALICE_UID,
      itemType: "QURAN",
      title: "سورة الفاتحة - آية 1",
      subtitle: "In the name of Allah",
      arabicText: "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
      referenceKey: "quran_1_1",
      createdAt: ts,
      updatedAt: ts,
    })
  );

  // Reject payload containing @DocumentId 'id'
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("bookmarks").doc("quran_1_2").set({
      id: "quran_1_2",
      userId: ALICE_UID,
      itemType: "QURAN",
      title: "سورة الفاتحة - آية 2",
      subtitle: "All praise is due to Allah",
      arabicText: "الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ",
      referenceKey: "quran_1_2",
      createdAt: ts,
      updatedAt: ts,
    })
  );

  // Valid adhkar_progress create & update
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("adhkar_progress").doc("morning").set({
      userId: ALICE_UID,
      categoryId: "morning",
      completedCount: 10,
      targetCount: 33,
      createdAt: ts,
      updatedAt: ts,
    })
  );
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("adhkar_progress").doc("morning").update({
      completedCount: 33,
      updatedAt: new Date(Date.now() - 1000),
    })
  );
});
