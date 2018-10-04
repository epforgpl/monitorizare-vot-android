package ro.code4.monitorizarevot.net;

import android.content.res.AssetManager;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ro.code4.monitorizarevot.net.model.BranchDetails;
import ro.code4.monitorizarevot.net.model.ResponseAnswerContainer;
import ro.code4.monitorizarevot.net.model.Section;
import ro.code4.monitorizarevot.net.model.User;
import ro.code4.monitorizarevot.net.model.response.Ack;
import ro.code4.monitorizarevot.net.model.response.ResponseNote;
import ro.code4.monitorizarevot.net.model.response.VersionResponse;
import ro.code4.monitorizarevot.net.model.response.question.QuestionResponse;

public class DumbApiService implements ApiService {

    private AssetManager assetManager;
    private String demoDir = "demo-pl";

    public void setAssetManager(AssetManager assetManager) {
        this.assetManager = assetManager;
    }

    public Call<Object> postAuth(final User user) {
        return new DumbCall<Object>() {
            public Response<Object> execute() {
                if (user.getPin().equals("1212")) {
                    return Response.error(400, ResponseBody.create(MediaType.parse("application/json"), ""));
                }

                return Response.<Object>success("{\"access_token\":\"accessTOKEN\"}");
            }
        };
    }

    public Call<List<Section>> getForm(final String formId) {
        return new DumbCall<List<Section>>() {
            public Response<List<Section>> execute() {
                List<Section> sections = new ArrayList<>();
                Gson gson = new Gson();

                sections.add(gson.fromJson(loadJson(demoDir + "/form" + formId + ".json"), Section.class));

                return Response.success(sections);
            }
        };
    }

    public Call<VersionResponse> getFormVersion() {
        return new DumbCall<VersionResponse>() {
            public Response<VersionResponse> execute() {
                Gson gson = new Gson();
                VersionResponse resp = gson.fromJson(loadJson(demoDir + "/versions.json"), VersionResponse.class);
                return Response.success(resp);
            }
        };
    }

    public Call<Ack> postBranchDetails(BranchDetails branchDetails) {
        return new DumbCall<Ack>() {
            @Override
            public Response<Ack> execute() {
                return Response.success(new Ack());
            }
        };
    }

    public Call<QuestionResponse> postQuestionAnswer(final ResponseAnswerContainer responseAnswer) {
        // TODO store responseAnswer.getReponseMapperList();

        return new DumbCall<QuestionResponse>() {
            @Override
            public Response<QuestionResponse> execute() {
                return Response.success(new QuestionResponse());
            }
        };
    }

    public Call<ResponseNote> postNote(MultipartBody.Part file, MultipartBody.Part countyCode, MultipartBody.Part branchNumber, MultipartBody.Part questionId, MultipartBody.Part description) {
        return new DumbCall<ResponseNote>() {
            @Override
            public Response<ResponseNote> execute() {
                return Response.success(new ResponseNote());
            }
        };
    }

    public abstract class DumbCall<T> implements Call<T> {
        public void enqueue(Callback<T> callback) {
        }

        public boolean isExecuted() {
            return false;
        }

        public void cancel() {
        }

        public boolean isCanceled() {
            return false;
        }

        public Call<T> clone() {
            return null;
        }

        public Request request() {
            return null;
        }
    }

    private String loadJson(String assetName) {
        try {
            return new Scanner(assetManager.open(assetName)).useDelimiter("\\Z").next();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
