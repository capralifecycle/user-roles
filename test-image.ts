import {
  createNetwork,
  createTestExecutor,
  startContainer,
  TestExecutor,
  waitForHttpOk,
  waitForPostgresAvailable,
} from "@capraconsulting/cals-cli";

async function main(executor: TestExecutor) {
  if (process.argv.length !== 3) {
    throw new Error(`Syntax: ${process.argv[0]} ${process.argv[1]} <image-id>`);
  }

  const imageId = process.argv[2];
  const network = await createNetwork(executor);

  const db = await startContainer({
    executor,
    network,
    alias: "db",
    imageId: "postgres:15.5",
    env: {
      POSTGRES_USER: "username",
      POSTGRES_PASSWORD: "password",
      POSTGRES_DB: "userrolesdb",
    },
  });

  await waitForPostgresAvailable({
    container: db,
    username: "username",
    dbname: "userrolesdb",
  });

  const service = await startContainer({
    executor,
    network,
    imageId,
    alias: "service",
    env: {
      DB_HOST: "db",
      DB_PORT: "5432",
    },
  });

  await waitForHttpOk({
    container: service,
    url: "service:8080/health",
  });
}

createTestExecutor().runWithCleanupTasks(main);
