const {onSchedule} = require('firebase-functions/v2/scheduler');
const {onDocumentWritten} = require('firebase-functions/v2/firestore');
const {onCall, HttpsError} = require('firebase-functions/v2/https');
const admin = require('firebase-admin');

admin.initializeApp();
const db = admin.firestore();

/**
 * Scheduled function that runs every hour to update event statuses
 */
exports.updateEventStatuses = onSchedule({
  schedule: 'every 1 hours',
  timeZone: 'America/Edmonton',
}, async (event) => {
  console.log('Running scheduled event status update...');

  try {
    const now = admin.firestore.Timestamp.now();
    const eventsRef = db.collection('org_events');
    const snapshot = await eventsRef.get();

    let updateCount = 0;
    const batch = db.batch();

    snapshot.forEach((doc) => {
      const data = doc.data();

      if (data.status === 'flagged') {
        return;
      }

      const regOpens = data.regOpens;
      const regCloses = data.regCloses;

      let newStatus = null;

      if (regOpens && regCloses) {
        if (now.toMillis() > regCloses.toMillis()) {
          newStatus = 'closed';
        } else {
          newStatus = 'open';
        }
      } else {
        newStatus = 'open';
      }

      if (data.status !== newStatus) {
        console.log(`Updating ${doc.id}: ${data.status} -> ${newStatus}`);
        batch.update(doc.ref, {status: newStatus});
        updateCount++;
      }
    });

    if (updateCount > 0) {
      await batch.commit();
      console.log(`Updated ${updateCount} event(s)`);
    } else {
      console.log('No events needed status updates');
    }

    return null;
  } catch (error) {
    console.error('Error updating event statuses:', error);
    return null;
  }
});

/**
 * Triggered when an event is created or modified
 */
exports.onEventWrite = onDocumentWritten('org_events/{eventId}', async (event) => {
  const after = event.data.after;
  const before = event.data.before;

  if (!after.exists) {
    return null;
  }

  const data = after.data();

  if (data.status === 'flagged') {
    return null;
  }

  const now = admin.firestore.Timestamp.now();
  const regOpens = data.regOpens;
  const regCloses = data.regCloses;

  let newStatus = null;

  if (regOpens && regCloses) {
    if (now.toMillis() > regCloses.toMillis()) {
      newStatus = 'closed';
    } else {
      newStatus = 'open';
    }
  } else {
    newStatus = 'open';
  }

  if (data.status !== newStatus &&
      (!before.exists || before.data().status === data.status)) {
    console.log(`Auto-updating event ${event.params.eventId}: ${data.status} -> ${newStatus}`);
    return after.ref.update({status: newStatus});
  }

  return null;
});

/**
 * Manual trigger from Android app
 */
exports.manualUpdateStatuses = onCall(async (request) => {
  console.log('Manual status update triggered');

  try {
    const now = admin.firestore.Timestamp.now();
    const eventsRef = db.collection('org_events');
    const snapshot = await eventsRef.get();

    let updateCount = 0;
    const batch = db.batch();

    snapshot.forEach((doc) => {
      const eventData = doc.data();

      if (eventData.status === 'flagged') {
        return;
      }

      const regOpens = eventData.regOpens;
      const regCloses = eventData.regCloses;

      let newStatus = null;

      if (regOpens && regCloses) {
        if (now.toMillis() > regCloses.toMillis()) {
          newStatus = 'closed';
        } else {
          newStatus = 'open';
        }
      } else {
        newStatus = 'open';
      }

      if (eventData.status !== newStatus) {
        batch.update(doc.ref, {status: newStatus});
        updateCount++;
      }
    });

    if (updateCount > 0) {
      await batch.commit();
    }

    return {
      success: true,
      message: `Updated ${updateCount} event(s)`,
    };
  } catch (error) {
    console.error('Error in manual update:', error);
    throw new HttpsError('internal', error.message);
  }
});