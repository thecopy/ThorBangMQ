class LogController < ApplicationController
  skip_before_filter :verify_authenticity_token, :if => Proc.new { |c| c.request.format == 'application/json' }


  def show
    @logs = LogEntry.all
  end

  def create
    LogEntry.create(log_params)
    head 201
  end

  def delete_all
    LogEntry.delete_all

    head 204
  end


  ## This stuff below here is not so nice, but it works :^)

  def get_session_by_id
    where_clause = "session_id = #{params[:id]}"

    @logs = LogEntry.where(where_clause).order('timestamp desc')

    if params[:download]
      result = ''
      @logs.each do |l|
        result += "#{l.timestamp} #{l.msg}\n"
      end
      send_data(result, :filename => "ThorBangMQ_session-#{params[:name]}.log")
      return
    end

    respond_to do |format|
      format.html  { render :template => 'log/show' }
      format.text  {

        result = ''
        @logs.each do |l|
          result += "#{l.timestamp} #{l.msg}\n"
        end
        send_data(result, :filename => "ThorBangMQ_session-#{params[:id]}.log")

      }
    end
  end

  def get_session_by_name
    where_clause = "name = '#{params[:name]}'"

    @logs = LogEntry.where(where_clause).order('timestamp desc')

    if params[:download]
      result = ''
      @logs.each do |l|
        result += "#{l.timestamp} #{l.msg}\n"
      end
      send_data(result, :filename => "ThorBangMQ_session-#{params[:name]}.log")
      return
    end

    respond_to do |format|
      format.html  { render :template => 'log/show' }
      format.text  {

        result = ''
        @logs.each do |l|
          result += "#{l.timestamp} #{l.msg}\n"
        end
        send_data(result, :filename => "ThorBangMQ_session-#{params[:name]}.log")

      }
    end
  end

  private
    def log_params
      params.require(:log_entry).permit(:msg, :session_id, :timestamp, :name)
    end
end
